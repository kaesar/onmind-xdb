package co.onmind.auth

import co.onmind.util.JsonMapper
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * Minimal RS256 JWT verifier against a JWKS document (Keycloak, Entra ID, ...).
 *
 * Sin dependencias extra: usa `java.net.http` para descargar el JWKS y
 * `java.security` (SHA256withRSA) para verificar la firma.
 *
 * Caché en memoria por URL (TTL 10 min). Si la descarga falla y hay una
 * entrada previa en caché, se reutiliza la anterior (resiliencia).
 */
object JwksValidator {

    /** Tiempo de vida de cada entrada JWKS en caché. */
    var cacheTtlMs: Long = 10 * 60 * 1000L

    /** Timeout HTTP para descargar el JWKS. */
    var httpTimeoutSeconds: Long = 5L

    private data class CachedJwks(val fetchedAt: Long, val keys: Map<String, RSAPublicKey>)

    private val cache = ConcurrentHashMap<String, CachedJwks>()

    /**
     * Verifica un JWT RS256 contra el JWKS de [jwksUrl].
     * Retorna los claims o null si es inválido.
     */
    fun verify(
        token: String,
        jwksUrl: String,
        issuer: String? = null,
        audience: String? = null,
        leewaySeconds: Long = 30
    ): Map<String, Any>? {
        val keys = keysForUrl(jwksUrl) ?: return null
        return verifyWithKeys(token, keys, issuer = issuer, audience = audience, leewaySeconds = leewaySeconds)
    }

    /** Verifica un JWT RS256 contra un documento JWKS en crudo (útil en tests). */
    fun verifyWithJwksJson(
        token: String,
        jwksJson: String,
        issuer: String? = null,
        audience: String? = null,
        leewaySeconds: Long = 30
    ): Map<String, Any>? {
        val keys = try {
            parseJwks(jwksJson)
        } catch (e: Exception) {
            return null
        }
        return verifyWithKeys(token, keys, issuer = issuer, audience = audience, leewaySeconds = leewaySeconds)
    }

    /** Verifica un JWT RS256 contra un mapa kid -> clave pública ya resuelto. */
    fun verifyWithKeys(
        token: String,
        keys: Map<String, RSAPublicKey>,
        issuer: String? = null,
        audience: String? = null,
        leewaySeconds: Long = 30
    ): Map<String, Any>? {
        return try {
            if (keys.isEmpty()) return null
            val parts = token.split(".")
            if (parts.size != 3) return null

            val header = decodeJson(parts[0]) ?: return null
            val payload = decodeJson(parts[1]) ?: return null
            val alg = header["alg"] as? String ?: return null
            if (!alg.equals("RS256", ignoreCase = true)) return null

            val kid = header["kid"] as? String
            val key: RSAPublicKey = if (kid != null) {
                keys[kid] ?: return null
            } else {
                if (keys.size != 1) return null
                keys.values.first()
            }

            val signingInput = "${parts[0]}.${parts[1]}".toByteArray(StandardCharsets.US_ASCII)
            val signature = try {
                Base64.getUrlDecoder().decode(parts[2])
            } catch (e: Exception) {
                return null
            }
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(key)
            sig.update(signingInput)
            if (!sig.verify(signature)) return null

            // Expiry (requerido, igual que JwtValidator HS256)
            val exp = (payload["exp"] as? Number)?.toLong() ?: return null
            val now = System.currentTimeMillis() / 1000
            if (now > exp + leewaySeconds) return null

            // Issuer opcional
            if (issuer != null) {
                val iss = payload["iss"] as? String ?: return null
                if (iss != issuer) return null
            }

            // Audience opcional
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

    /** Parsea un documento JWKS y retorna kid -> clave pública RSA (solo kty=RSA). */
    fun parseJwks(jwksJson: String): Map<String, RSAPublicKey> {
        @Suppress("UNCHECKED_CAST")
        val doc = JsonMapper.instance.readValue(jwksJson, Map::class.java) as Map<String, Any>
        val rawKeys = doc["keys"] as? List<*> ?: return emptyMap()
        val out = LinkedHashMap<String, RSAPublicKey>()
        val keyFactory = KeyFactory.getInstance("RSA")
        for (raw in rawKeys) {
            val entry = raw as? Map<*, *> ?: continue
            if ((entry["kty"] as? String)?.equals("RSA", ignoreCase = true) != true) continue
            val n = entry["n"] as? String ?: continue
            val e = entry["e"] as? String ?: continue
            val modulus = BigInteger(1, Base64.getUrlDecoder().decode(n))
            val exponent = BigInteger(1, Base64.getUrlDecoder().decode(e))
            val publicKey = keyFactory.generatePublic(RSAPublicKeySpec(modulus, exponent)) as RSAPublicKey
            val kid = entry["kid"] as? String ?: ""
            out[kid] = publicKey
        }
        return out
    }

    /** Descarga y parsea un JWKS (sin caché). Retorna null si falla. */
    fun fetchJwks(jwksUrl: String): Map<String, RSAPublicKey>? {
        return try {
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(httpTimeoutSeconds))
                .build()
            val request = HttpRequest.newBuilder(URI.create(jwksUrl))
                .timeout(Duration.ofSeconds(httpTimeoutSeconds))
                .header("Accept", "application/json")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return null
            parseJwks(response.body())
        } catch (e: Exception) {
            null
        }
    }

    /** Limpia la caché JWKS (útil en tests). */
    fun clearCache() {
        cache.clear()
    }

    private fun keysForUrl(jwksUrl: String): Map<String, RSAPublicKey>? {
        val now = System.currentTimeMillis()
        val cached = cache[jwksUrl]
        if (cached != null && now - cached.fetchedAt < cacheTtlMs && cached.keys.isNotEmpty()) {
            return cached.keys
        }
        val fresh = fetchJwks(jwksUrl)
        if (fresh != null && fresh.isNotEmpty()) {
            cache[jwksUrl] = CachedJwks(now, fresh)
            return fresh
        }
        // Resiliencia: reutilizar la entrada anterior aunque esté caducada.
        if (cached != null && cached.keys.isNotEmpty()) return cached.keys
        return null
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
}
