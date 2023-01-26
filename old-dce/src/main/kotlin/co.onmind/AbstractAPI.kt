package co.onmind

import io.javalin.http.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.buildJsonObject
import java.time.LocalDateTime
import java.util.Random
import java.util.UUID

open class AbstractAPI() {

    val mapper = jacksonObjectMapper()

    fun Context.getBodyAsJson() = mapper.readValue(this.body(), MutableMap::class.java)

    fun Context.success(result: Map<String, Any?>) =
        this.contentType("application/json")
            .result(mapper.writeValueAsString(result))

    fun Context.success(result: List<MutableMap<String, Any?>>?) =
        this.contentType("application/json")
            .result("{\"success\":true,\"status\":200,\"data\":" + mapper.writeValueAsString(result?.map { it.mapKeys { it.key.lowercase() } }) + "}")
        /*this.json(object {
            val success = true
            val status = 200
            val data = result?.map { it.mapKeys { it.key.lowercase() } }
        })*/

    fun Context.success(message: String) =
        this.contentType("application/json")
            .result("{\"success\":true,\"status\":200,\"message\":\"$message\"}")
        /*this.json(object {
            val success = true
            val status = 200
            val message = message
        })*/

    fun Context.error(message: String, status: Int = 403) =
        this.contentType("application/json")
            .result("{\"success\":false,\"status\":$status,\"message\":\"$message\"}")
        /*this.json(object {
            val success = false
            val status = status
            val message = message
        })*/

    fun Int.asLeadingZeros(n: Int): String {
        var result = this.toString()
        val size = result.length
        if (size < n)
            for (i in size..n-1)
                result = "0" + result
        return result
    }

    fun Long.asLeadingZeros(n: Long): String {
        var result = this.toString()
        val size = result.length
        if (size < n)
            for (i in size..n-1)
                result = "0" + result
        return result
    }

    fun String?.weight(): Int {
        if (this == null) return 0
        var j = 0
        for (i in 0 until this.length)
            j += this.codePointAt(i)
        return j
    }

    fun String.salt(n: Int = 5): String? {
        if (this.isEmpty()) return this
        var salt = this.weight().toString()
        val size = salt.length
        if (size < n)
            salt = "0" + salt
        else if (size > n)
            salt = salt.substring(size - n, size)
        return salt
    }

    fun putID(kind: Int = 0, user: String? = null, stamp: LocalDateTime? = null): String {
        if (kind == 0)                           // Safe UUID but big size and a little bit less of performance
            return UUID.randomUUID().toString()  // UUID => 36 chars

        // UUID Short = ID from Server LocalDateTime + bite => 16 chars (kind == 1 || kind >= 16)
        val t = if (stamp == null) LocalDateTime.now() else stamp
        val now = t.toString().replace("T"," ")
        val days = t.dayOfYear.asLeadingZeros(3)
        val randomGenerator = Random()
        var result = now.substring(2,4) + days + now.substring(11,13) + now.substring(14,16) + now.substring(17,19)
        var bite: String?

        if (kind == 1 || kind == 16) {  // Risky by math but possible for non big data, even under C10K
            if (user != null && user.isNotEmpty())
                bite = user.salt(1) + randomGenerator.nextInt(1000).asLeadingZeros(3)
            else
                bite = randomGenerator.nextInt(10000).asLeadingZeros(4)

            result += now.substring(22,23) + bite
        }
        else if (kind == 2 || kind == 18) {  // Just little bit risky in accuracy
            if (user != null && user.isNotEmpty())
                bite = user.salt(2) + randomGenerator.nextInt(10000).asLeadingZeros(4)
            else
                bite = randomGenerator.nextInt(1000000).asLeadingZeros(6)

            result += now.substring(22,23) + bite
        }
        else {  // Almost impossible of repeat or good level of accuracy
            if (user != null && user.isNotEmpty())
                bite = user.salt(3) + randomGenerator.nextInt(100000).asLeadingZeros(5)
            else
                bite = randomGenerator.nextInt(100000000).asLeadingZeros(8)

            if (kind == 3 || kind == 20)
                result += now.substring(22,23) + bite
            else  // 22 (Great Level)
                result += now.substring(20,23) + bite
        }

        return result
    }
}