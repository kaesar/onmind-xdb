package co.onmind.util

import java.time.LocalDateTime
import java.util.Random
import java.util.UUID

/**
 * ID generation shared by the ABC core (AbstractAPI.putID) and the FILE API.
 * Keeps the XDB numeric id algorithm in one place.
 */
object AbstractIds {

    fun Int.asLeadingZeros(n: Int): String {
        var result = this.toString()
        val size = result.length
        if (size < n)
            for (i in size until n)
                result = "0$result"
        return result
    }

    fun String.weight(): Int {
        if (this.isEmpty()) return 0
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

    /** XDB numeric id: yyddMMddHHmmss + millis + user-salt + random bite. */
    fun putId(user: String? = null, stamp: LocalDateTime? = null): String {
        val t = stamp ?: LocalDateTime.now()
        val now = t.toString().replace("T", " ")
        val days = t.dayOfYear.asLeadingZeros(3)
        val randomGenerator = Random()
        var result = now.substring(2, 4) + days + now.substring(11, 13) + now.substring(14, 16) + now.substring(17, 19)
        val bite = if (user?.isNotEmpty() == true)
            user.salt(3) + randomGenerator.nextInt(100000).asLeadingZeros(5)
        else
            randomGenerator.nextInt(100000000).asLeadingZeros(8)
        result += now.substring(20, 23) + bite
        return result
    }

    fun uuid(): String = UUID.randomUUID().toString()
}