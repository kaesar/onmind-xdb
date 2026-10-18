package co.onmind

import co.onmind.kv.DynamoPlug
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Offline checks for the DynamoDB backend (full profile only — this file lives
 * in src/full/test, which is not compiled in the lite profile).
 *
 * No AWS call is made: [DynamoPlug.put]/[DynamoPlug.delete]/[DynamoPlug.get]
 * operate on the in-process write buffer until `commit()`.
 */
class DynamoPlugTest {

    private fun plug() = DynamoPlug().apply { init("onmind-xdb", "us-west-2", false, false) }

    @Test
    fun `uses the table argument, not the region`() {
        // Regression: init used to read params[1] (the region) as the table name.
        val p = plug()
        p.put("abc~kit~box", "{\"v\":1}")
        assertEquals("{\"v\":1}", p.get("abc~kit~box"))
    }

    @Test
    fun `read-your-writes from the buffer`() {
        val p = plug()
        p.put("k1", "v1")
        p.put("k2", "v2")
        assertEquals("v1", p.get("k1"))
        assertEquals("v2", p.get("k2"))
    }

    @Test
    fun `delete shadows a buffered put`() {
        val p = plug()
        p.put("k", "v")
        p.delete("k")
        assertNull(p.get("k"))
    }

    @Test
    fun `oversized value is rejected before any network call`() {
        val p = plug()
        val big = "x".repeat(400_001)
        val ex = assertThrows(IllegalArgumentException::class.java) { p.put("k", big) }
        assertTrue(ex.message!!.contains("400 KB"))
    }

    @Test
    fun `key mapping round-trips`() {
        val p = plug()
        assertEquals("a#kit#box", p.toExternalKey("a~kit~box"))
        assertEquals("a~kit~box", p.toInternalKey("a#kit#box"))
    }
}
