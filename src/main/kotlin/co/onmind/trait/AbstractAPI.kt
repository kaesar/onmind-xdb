package co.onmind.trait

import co.onmind.io.AbcBack
import org.http4k.core.*
import org.http4k.lens.BiDiBodyLens
import org.http4k.format.Jackson.auto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime
import java.util.Random
import java.util.UUID

open class AbstractAPI() {

    val mapper = jacksonObjectMapper()
    val apiSend: BiDiBodyLens<AbcBack> = Body.auto<AbcBack>().toLens()

    fun sendSuccess(result: Map<String, Any?>) =
        Response(Status.OK)
            .header("Content-Type",ContentType.APPLICATION_JSON.value)
            .body(mapper.writeValueAsString(result))

    fun sendSuccess(result: List<MutableMap<String, Any?>>?) =
        apiSend.inject(AbcBack(true, Status.OK.code.toString(), result, result?.size), Response(Status.OK))

    fun sendSuccess(message: String) =
        apiSend.inject(AbcBack(true, Status.OK.code.toString(), message), Response(Status.OK))

    fun sendError(message: String) =
        apiSend.inject(AbcBack(false, Status.BAD_REQUEST.code.toString(), message), Response(Status.BAD_REQUEST))

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

    fun String.salt(n: Int = 5): String {
        if (this.isEmpty()) return this
        var salt = this.weight().toString()
        val size = salt.length
        if (size < n)
            salt = "0$salt"
        else if (size > n)
            salt = salt.substring(size - n, size)
        return salt
    }

    fun putID(kind: Int = 0, user: String? = null, stamp: LocalDateTime? = null): String {
        if (kind == 0)                           // Safe UUID but big size and a little bit less of performance
            return UUID.randomUUID().toString()  // UUID => 36 chars

        // UUID Short = ID from Server LocalDateTime + bite => 16 chars (kind == 1 || kind >= 16)
        val t = stamp ?: LocalDateTime.now()
        val now = t.toString().replace("T"," ")
        val days = t.dayOfYear.asLeadingZeros(3)
        val randomGenerator = Random()
        var result = now.substring(2,4) + days + now.substring(11,13) + now.substring(14,16) + now.substring(17,19)
        val bite: String?

        if (kind == 1 || kind == 16) {  // Risky by math but possible for non big data, even under C10K
            bite = if (user?.isNotEmpty() == true)
                user.salt(1) + randomGenerator.nextInt(1000).asLeadingZeros(3)
            else
                randomGenerator.nextInt(10000).asLeadingZeros(4)

            result += now.substring(22,23) + bite
        }
        else if (kind == 2 || kind == 18) {  // Just little bit risky in accuracy
            bite = if (user?.isNotEmpty() == true)
                user.salt(2) + randomGenerator.nextInt(10000).asLeadingZeros(4)
            else
                randomGenerator.nextInt(1000000).asLeadingZeros(6)

            result += now.substring(22,23) + bite
        }
        else {  // Almost impossible of repeat or good level of accuracy
            bite = if (user?.isNotEmpty() == true)
                user.salt(3) + randomGenerator.nextInt(100000).asLeadingZeros(5)
            else
                randomGenerator.nextInt(100000000).asLeadingZeros(8)

            if (kind == 3 || kind == 20)
                result += now.substring(22,23) + bite
            else  // 22 (Great Level)
                result += now.substring(20,23) + bite
        }

        return result
    }
}