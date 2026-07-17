package co.onmind.auth

import co.onmind.db.DBKey
import co.onmind.db.RDB
import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import co.onmind.util.CoherenceStore
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.cookie.invalidateCookie
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Passwordless OTP-via-email authentication (parity with OnMind-WDB OTP Mail).
 *
 * Flow:
 * 1. User submits email → OTP generated (6 digits, TTL 5 min) and emailed
 * 2. User submits code → constant-time verify, max 3 attempts
 * 3. Signed HttpOnly cookie session (7 days) injects X-Auth-User
 *
 * Users are resolved / auto-registered in xykey (email as key02).
 */
class OTPMailPlug(
    private val smtpHost: String = "localhost",
    private val smtpPort: String = "1025",
    private val smtpUser: String = "",
    private val smtpPass: String = "",
    private val fromEmail: String = "xdb@localhost",
    private val sessionKey: String = "change-me-otp-session-key",
    private val autoRegister: Boolean = true
) : AuthProvider {

    private val xdb = RDB()
    private val otpStore = ConcurrentHashMap<String, OtpEntry>()
    private val secureRandom = SecureRandom()

    init {
        val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "xdb-otp-cleaner").apply { isDaemon = true }
        }
        scheduler.scheduleAtFixedRate({
            val now = Instant.now()
            otpStore.entries.removeIf { now.isAfter(it.value.expiresAt) }
        }, 1, 1, TimeUnit.MINUTES)

        println("Auth Provider Configured: OTP Mail ($fromEmail@$smtpHost:$smtpPort)")
    }

    // ------------------------------------------------------------------
    // AuthProvider
    // ------------------------------------------------------------------

    override fun authenticate(request: Request): AuthResult {
        val email = readSessionEmail(request) ?: return AuthResult.Failure("No OTP session")
        return AuthResult.Success(
            AuthUser(id = email, email = email, name = email, roles = listOf("USER"))
        )
    }

    override fun filter() = Filter { next ->
        { request ->
            val path = request.uri.path
            if (isPublicAuthPath(path)) {
                next(request)
            } else {
                when (val result = authenticate(request)) {
                    is AuthResult.Success -> next(request.header("X-Auth-User", result.user.id))
                    is AuthResult.Failure -> unauthorized(request, result.reason)
                }
            }
        }
    }

    /** Login / send / verify / logout routes. */
    fun routes() = routes(
        "/auth/otpmail/login" bind Method.GET to { loginHandler(it) },
        "/auth/otpmail/send" bind Method.POST to { sendHandler(it) },
        "/auth/otpmail/verify" bind Method.POST to { verifyHandler(it) },
        "/auth/otpmail/logout" bind Method.GET to { logoutHandler() },
        "/auth/otpmail/logout" bind Method.POST to { logoutHandler() }
    )

    // ------------------------------------------------------------------
    // Handlers
    // ------------------------------------------------------------------

    private fun loginHandler(request: Request): Response {
        val error = request.uri.query?.let { q ->
            q.split("&").mapNotNull { pair ->
                val p = pair.split("=", limit = 2)
                if (p.size == 2 && p[0] == "error") urlDecode(p[1]) else null
            }.firstOrNull()
        }
        return html(loginPage(error))
    }

    private fun sendHandler(request: Request): Response {
        val form = parseForm(request)
        val email = form["email"]?.trim()?.lowercase().orEmpty()
        if (email.isEmpty()) {
            return redirect("/auth/otpmail/login?error=Email+is+required")
        }

        try {
            val exists = lookupUser(email)
            if (!exists) {
                if (autoRegister) {
                    createUser(email)
                    println("OTP auto-registered new user: $email")
                } else {
                    return redirect("/auth/otpmail/login?error=Unknown+user")
                }
            }
        } catch (e: Exception) {
            println("OTP user lookup/register failed for $email: ${e.message}")
            return redirect("/auth/otpmail/login?error=Internal+error")
        }

        val code = generateOtp()
        otpStore[email] = OtpEntry(
            code = code,
            expiresAt = Instant.now().plusSeconds(OTP_TTL_SECONDS),
            attempts = 0
        )

        try {
            sendOtpEmail(email, code)
            println("OTP email sent to $email")
        } catch (e: Exception) {
            // Dev-friendly: still show verify page; code is in logs (and on page for localhost SMTP)
            println("OTP email send failed (verify page still shown): ${e.message} — code for $email: $code")
        }

        val codeHint = if (smtpHost == "localhost" || smtpHost == "127.0.0.1") {
            """<div style="margin-top:.75rem;padding:.5rem;background:#fef3c7;border-radius:.375rem;font-size:.75rem;color:#92400e;text-align:center">Dev mode: code = <strong>${escapeHtml(code)}</strong></div>"""
        } else ""

        return html(verifyPage(email, codeHint))
    }

    private fun verifyHandler(request: Request): Response {
        val form = parseForm(request)
        val email = form["email"]?.trim()?.lowercase().orEmpty()
        val code = form["code"]?.trim().orEmpty()

        if (email.isEmpty() || code.isEmpty()) {
            return redirect("/auth/otpmail/login?error=Missing+fields")
        }

        val entry = otpStore[email]
            ?: return redirect("/auth/otpmail/login?error=No+code+requested")

        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(email)
            return redirect("/auth/otpmail/login?error=Code+expired")
        }

        if (entry.attempts >= OTP_MAX_ATTEMPTS) {
            otpStore.remove(email)
            return redirect("/auth/otpmail/login?error=Too+many+attempts")
        }

        entry.attempts++

        if (!constantTimeEquals(entry.code, code)) {
            val remaining = OTP_MAX_ATTEMPTS - entry.attempts
            val err = if (remaining > 0) {
                "Invalid code ($remaining attempts remaining)"
            } else {
                "Invalid code"
            }
            return html(verifyPage(email, """<div class="error">${escapeHtml(err)}</div>"""))
        }

        otpStore.remove(email)

        val cookie = buildSessionCookie(email)
        println("OTP user authenticated: $email")
        return Response(Status.FOUND)
            .header("Location", "/app/")
            .cookie(cookie)
    }

    private fun logoutHandler(): Response {
        return Response(Status.FOUND)
            .header("Location", "/")
            .invalidateCookie(COOKIE_NAME)
    }

    // ------------------------------------------------------------------
    // User store (xykey)
    // ------------------------------------------------------------------

    private fun lookupUser(email: String): Boolean {
        val safe = escapeSql(email)
        val query = "SELECT id FROM xykey WHERE LOWER(key02)='$safe' LIMIT 1"
        val rows = xdb.forQuery(query)
        return !rows.isNullOrEmpty()
    }

    private fun createUser(email: String) {
        val scheme = "USER"
        val now = LocalDateTime.now()
        val id = UUID.randomUUID().toString().replace("-", "").take(16)
        val code = "${email.uppercase()}.$scheme"
        val dbKey = DBKey()
        val map = mutableMapOf<String, Any?>(
            "keyxy" to scheme,
            "key01" to code,
            "key02" to email,
            "key03" to email.uppercase(),
            "key09" to "90",
            "key16" to "EN",
            "key20" to "OK",
            "keyon" to now.toString()
        )
        val query = dbKey.getInsert(map, scheme, email, id, now, null)
        xdb.forUpdate(query)
        CoherenceStore.incrementMemoryCount("key")
        val selectQuery = "SELECT * FROM xykey WHERE id='$id'"
        val rows = xdb.forQuery(selectQuery)
        val row = rows?.get(0) ?: mutableMapOf()
        xdb.savePointKey(row)
    }

    // ------------------------------------------------------------------
    // OTP + session
    // ------------------------------------------------------------------

    private fun generateOtp(): String {
        val sb = StringBuilder(OTP_LEN)
        repeat(OTP_LEN) {
            sb.append(secureRandom.nextInt(10))
        }
        return sb.toString()
    }

    private fun buildSessionCookie(email: String): Cookie {
        val expiry = Instant.now().epochSecond + SESSION_MAX_AGE_SECONDS
        val payload = "$email|$expiry"
        val sig = hmac(payload)
        val value = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("$payload|$sig".toByteArray(StandardCharsets.UTF_8))
        return Cookie(
            name = COOKIE_NAME,
            value = value,
            maxAge = SESSION_MAX_AGE_SECONDS,
            path = "/",
            httpOnly = true
        )
    }

    private fun readSessionEmail(request: Request): String? {
        val raw = request.cookies().firstOrNull { it.name == COOKIE_NAME }?.value ?: return null
        return try {
            val decoded = String(Base64.getUrlDecoder().decode(raw), StandardCharsets.UTF_8)
            val parts = decoded.split("|")
            if (parts.size != 3) return null
            val email = parts[0]
            val expiry = parts[1].toLongOrNull() ?: return null
            val sig = parts[2]
            if (Instant.now().epochSecond > expiry) return null
            val expected = hmac("$email|$expiry")
            if (!constantTimeEquals(expected, sig)) return null
            if (email.isBlank()) return null
            email
        } catch (_: Exception) {
            null
        }
    }

    private fun hmac(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(sessionKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val bytes = mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val ba = a.toByteArray(StandardCharsets.UTF_8)
        val bb = b.toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.isEqual(ba, bb)
    }

    // ------------------------------------------------------------------
    // SMTP (JDK only — plain for Mailpit, STARTTLS + AUTH PLAIN when credentials set)
    // ------------------------------------------------------------------

    private fun sendOtpEmail(email: String, code: String) {
        val subject = "Your XDB login code"
        val body = """
            <!DOCTYPE html>
            <html>
            <body style="font-family:system-ui,sans-serif;padding:2rem">
              <h2>OnMind-XDB</h2>
              <p>Use the code below to sign in. It expires in 5 minutes.</p>
              <div style="font-size:2rem;font-weight:bold;letter-spacing:0.25em;padding:1rem 0">$code</div>
              <p style="color:#64748b">If you did not request this code, ignore this message.</p>
            </body>
            </html>
        """.trimIndent()

        val port = smtpPort.toIntOrNull() ?: 25
        var socket: Socket = Socket(smtpHost, port)
        socket.soTimeout = 15_000

        try {
            var reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            var writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), false)

            fun readReply(): String {
                val sb = StringBuilder()
                while (true) {
                    val line = reader.readLine() ?: break
                    sb.appendLine(line)
                    if (line.length >= 4 && line[3] == ' ') break
                }
                return sb.toString()
            }

            fun writeCmd(cmd: String) {
                writer.print(cmd)
                writer.print("\r\n")
                writer.flush()
            }

            fun expectOk(reply: String, prefix: String) {
                if (!reply.startsWith(prefix)) {
                    throw IllegalStateException("SMTP unexpected reply (want $prefix): ${reply.trim()}")
                }
            }

            expectOk(readReply(), "220")
            writeCmd("EHLO onmind-xdb")
            val ehlo = readReply()
            expectOk(ehlo, "250")

            if (smtpUser.isNotBlank()) {
                if (ehlo.contains("STARTTLS", ignoreCase = true)) {
                    writeCmd("STARTTLS")
                    expectOk(readReply(), "220")
                    val ssl = (SSLSocketFactory.getDefault() as SSLSocketFactory)
                        .createSocket(socket, smtpHost, port, true) as SSLSocket
                    ssl.startHandshake()
                    socket = ssl
                    reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                    writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), false)
                    writeCmd("EHLO onmind-xdb")
                    expectOk(readReply(), "250")
                }

                val authPlain = Base64.getEncoder().encodeToString(
                    "\u0000$smtpUser\u0000$smtpPass".toByteArray(StandardCharsets.UTF_8)
                )
                writeCmd("AUTH PLAIN $authPlain")
                expectOk(readReply(), "235")
            }

            writeCmd("MAIL FROM:<$fromEmail>")
            expectOk(readReply(), "250")
            writeCmd("RCPT TO:<$email>")
            expectOk(readReply(), "250")
            writeCmd("DATA")
            expectOk(readReply(), "354")

            val data = buildString {
                append("From: $fromEmail\r\n")
                append("To: $email\r\n")
                append("Subject: $subject\r\n")
                append("MIME-Version: 1.0\r\n")
                append("Content-Type: text/html; charset=UTF-8\r\n")
                append("\r\n")
                append(body)
                append("\r\n.\r\n")
            }
            writer.print(data)
            writer.flush()
            expectOk(readReply(), "250")
            writeCmd("QUIT")
            readReply()
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {
            }
        }
    }

    // ------------------------------------------------------------------
    // HTML pages
    // ------------------------------------------------------------------

    private fun loginPage(error: String?): String {
        val errHtml = if (!error.isNullOrBlank()) {
            """<div class="error">${escapeHtml(error)}</div>"""
        } else ""
        return LOGIN_PAGE.replace("{ERROR}", errHtml)
    }

    private fun verifyPage(email: String, errorOrHint: String): String {
        return VERIFY_PAGE
            .replace("{EMAIL}", escapeHtml(email))
            .replace("{ERROR}", errorOrHint)
    }

    private fun html(body: String): Response =
        Response(Status.OK).header("Content-Type", "text/html; charset=utf-8").body(body)

    private fun redirect(location: String): Response =
        Response(Status.FOUND).header("Location", location)

    private fun unauthorized(request: Request, reason: String): Response {
        val path = request.uri.path
        val accept = request.header("Accept").orEmpty()
        val wantsHtml = path.startsWith("/app") || path == "/" || accept.contains("text/html")
        return if (wantsHtml) {
            redirect("/auth/otpmail/login")
        } else {
            Response(Status.UNAUTHORIZED)
                .header("Content-Type", "application/json")
                .body("""{"error":"unauthorized","reason":"${escapeJson(reason)}"}""")
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun isPublicAuthPath(path: String): Boolean =
        path == "/auth/otpmail/login" ||
            path == "/auth/otpmail/send" ||
            path == "/auth/otpmail/verify" ||
            path == "/auth/otpmail/logout"

    private fun parseForm(request: Request): Map<String, String> {
        val body = request.bodyString()
        if (body.isBlank()) return emptyMap()
        return body.split("&").mapNotNull { pair ->
            val parts = pair.split("=", limit = 2)
            if (parts.size != 2) return@mapNotNull null
            urlDecode(parts[0]) to urlDecode(parts[1])
        }.toMap()
    }

    private fun urlDecode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8)

    private fun escapeHtml(value: String): String =
        value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun escapeSql(value: String): String =
        value.replace("'", "''")

    private data class OtpEntry(
        val code: String,
        val expiresAt: Instant,
        var attempts: Int
    )

    companion object {
        private const val COOKIE_NAME = "xdb-otp-session"
        private const val OTP_LEN = 6
        private const val OTP_TTL_SECONDS = 5L * 60
        private const val OTP_MAX_ATTEMPTS = 3
        private const val SESSION_MAX_AGE_SECONDS = 7L * 24 * 60 * 60

        private val LOGIN_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Sign in — OnMind-XDB</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:system-ui,-apple-system,sans-serif;background:#f8fafc;display:flex;align-items:center;justify-content:center;min-height:100vh}
.card{background:white;border-radius:.5rem;box-shadow:0 1px 3px rgba(0,0,0,.1);padding:2rem;width:100%;max-width:400px}
h1{font-size:1.5rem;font-weight:700;color:#1e293b;margin-bottom:.25rem}
p{color:#64748b;font-size:.875rem;margin-bottom:1.5rem}
label{display:block;font-size:.875rem;font-weight:500;color:#334155;margin-bottom:.5rem}
input{width:100%;padding:.5rem .75rem;border:1px solid #e2e8f0;border-radius:.375rem;font-size:1rem;outline:none}
input:focus{border-color:#3b82f6;box-shadow:0 0 0 3px rgba(59,130,246,.1)}
button{width:100%;padding:.5rem 1rem;background:#2563eb;color:white;border:none;border-radius:.375rem;font-size:.875rem;font-weight:500;cursor:pointer;margin-top:1rem}
button:hover{background:#1d4ed8}
.error{color:#dc2626;font-size:.875rem;margin-top:.75rem;text-align:center}
</style>
</head>
<body>
<div class="card">
  <h1>Sign in to XDB</h1>
  <p>Enter your email to receive a one-time code</p>
  <form method="POST" action="/auth/otpmail/send">
    <label for="email">Email address</label>
    <input type="email" id="email" name="email" placeholder="you@example.com" required autofocus>
    <button type="submit">Send code</button>
  </form>
  {ERROR}
</div>
</body>
</html>
        """.trimIndent()

        private val VERIFY_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Verify code — OnMind-XDB</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:system-ui,-apple-system,sans-serif;background:#f8fafc;display:flex;align-items:center;justify-content:center;min-height:100vh}
.card{background:white;border-radius:.5rem;box-shadow:0 1px 3px rgba(0,0,0,.1);padding:2rem;width:100%;max-width:400px}
h1{font-size:1.5rem;font-weight:700;color:#1e293b;margin-bottom:.25rem}
p{color:#64748b;font-size:.875rem;margin-bottom:1.5rem}
label{display:block;font-size:.875rem;font-weight:500;color:#334155;margin-bottom:.5rem}
input{width:100%;padding:.5rem .75rem;border:1px solid #e2e8f0;border-radius:.375rem;font-size:1.5rem;outline:none;text-align:center;letter-spacing:.25em}
input:focus{border-color:#3b82f6;box-shadow:0 0 0 3px rgba(59,130,246,.1)}
button{width:100%;padding:.5rem 1rem;background:#2563eb;color:white;border:none;border-radius:.375rem;font-size:.875rem;font-weight:500;cursor:pointer;margin-top:1rem}
button:hover{background:#1d4ed8}
a{display:block;text-align:center;margin-top:1rem;color:#3b82f6;font-size:.875rem;text-decoration:none}
a:hover{text-decoration:underline}
.error{color:#dc2626;font-size:.875rem;margin-top:.75rem;text-align:center}
</style>
</head>
<body>
<div class="card">
  <h1>Check your email</h1>
  <p>A code was sent to <strong>{EMAIL}</strong></p>
  <form method="POST" action="/auth/otpmail/verify">
    <input type="hidden" name="email" value="{EMAIL}">
    <label for="code">Enter the 6-digit code</label>
    <input type="text" id="code" name="code" placeholder="000000" maxlength="6" inputmode="numeric" pattern="[0-9]{6}" required autofocus>
    <button type="submit">Verify</button>
  </form>
  <a href="/auth/otpmail/login">Use a different email</a>
  {ERROR}
</div>
</body>
</html>
        """.trimIndent()
    }
}
