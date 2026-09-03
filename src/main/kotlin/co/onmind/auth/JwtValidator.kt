package co.onmind.auth

import co.onmind.util.JsonMapper
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

/**
 * Minimal HS256 JWT verifier for resource-server authentication.
 *
 * OnMind-UID signs access/id tokens with HS256 by default (shared `jwt.secret`).
 * This verifier checks: signature, `exp`, optional `iss`, and optional `aud`.
 * It keeps the consumer (XDB) free of an extra JWT dependency.
 */
object JwtValidator {

    /**
     * Verify an HS256 JWT and return its claims, or null if invalid.
     *
     * @param token    the JWT string
     * @param secret   the HMAC shared secret
     * @param issuer   if non-null, the `iss` claim must match
     * @param audience if non-null, the `aud` claim must contain it
     * @param leewaySeconds optional clock skew tolerance
     */
    fun verify(token: String, secret: String, issuer: String? = null, audience: String? = null, leewaySeconds: Long = 30): Map<String, Any>? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val header = decodeJson(parts[0]) ?: return null
            val payload = decodeJson(parts[1]) ?: return null
            val algorithm = header["alg"] as? String ?: return null
            if (!algorithm.equals("HS256", ignoreCase = true)) return null

            // Signature verification (constant-time compare)
            val signedData = "${parts[0]}.${parts[1]}"
            val expected = hmac(secret, signedData)
            val signature = parts[2]
            if (!constantTimeEquals(expected, signature)) return null

            // Expiry
            val exp = (payload["exp"] as? Number)?.toLong() ?: return null
            val now = System.currentTimeMillis() / 1000
            if (now > exp + leewaySeconds) return null

            // Issuer
            if (issuer != null) {
                val iss = payload["iss"] as? String ?: return null
                if (iss != issuer) return null
            }

            // Audience
            if (audience != null) {
                val aud = payload["aud"]
                val ok = when (aud) {
                    is String -> aud == audience
                    is List<*> -> aud.any { it?.toString() == audience }
                    else -> false
                }
                if (!ok) return null
            }

            payload
        } catch (e: Exception) {
            null
        }
    }

    private fun hmac(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.toByteArray(StandardCharsets.UTF_8)))
    }

    private fun decodeJson(segment: String): Map<String, Any>? {
        val bytes = try {
            Base64.getUrlDecoder().decode(segment)
        } catch (e: Exception) {
            return null
        }
        return try {
            @Suppress("UNCHECKED_CAST")
            JsonMapper.instance.readValue(String(bytes, StandardCharsets.UTF_8), Map::class.java) as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var x = 0
        for (i in a.indices) x = x or (a[i].code xor b[i].code)
        return x == 0
    }
}
