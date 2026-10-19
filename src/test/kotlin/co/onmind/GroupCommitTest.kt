package co.onmind

import co.onmind.kv.GroupCommitStore
import co.onmind.kv.KVStoreFactory
import co.onmind.kv.MVStorePlug
import co.onmind.trait.KVStore
import co.onmind.util.Durability
import co.onmind.util.Rote
import onmindxdb
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Checks for `kv.durability` (sync | group | async) and [GroupCommitStore].
 *
 * Standard JUnit 5 tests, executed by `./gradlew test`.
 */
class GroupCommitTest {

    /** In-memory store that stages writes and only "persists" them on commit(). */
    private class FakeStore : KVStore {
        override val supportsGroupCommit = true

        private val staged = LinkedHashMap<String, String?>()
        private val durable = ConcurrentHashMap<String, String>()
        val commits = AtomicInteger()

        @Volatile var commitDelayMs = 0L

        @Volatile var failCommit = false

        @Synchronized
        override fun put(key: String, value: String) { staged[key] = value }

        @Synchronized
        override fun delete(key: String) { staged[key] = null }

        @Synchronized
        override fun get(key: String): String? = if (staged.containsKey(key)) staged[key] else durable[key]

        override fun init(vararg params: Any?) {}

        override fun commit() {
            if (commitDelayMs > 0) Thread.sleep(commitDelayMs)
            if (failCommit) throw RuntimeException("commit failed")
            val snapshot: Map<String, String?>
            synchronized(this) {
                snapshot = LinkedHashMap(staged)
                staged.clear()
            }
            for ((k, v) in snapshot) if (v == null) durable.remove(k) else durable[k] = v
            commits.incrementAndGet()
        }

        override fun close() {}

        override fun forEach(action: (String, String) -> Unit) { durable.forEach { (k, v) -> action(k, v) } }

        fun durableValue(key: String): String? = durable[key]
    }

    private fun props(vararg pairs: Pair<String, String>) = Properties().apply {
        pairs.forEach { (k, v) -> setProperty(k, v) }
    }

    @Test
    fun `kv durability parser is tolerant and defaults to sync`() {
        assertEquals(Durability.SYNC, Rote.durabilityProp(Properties(), "kv.durability"), "missing -> sync")
        assertEquals(Durability.SYNC, Rote.durabilityProp(props("kv.durability" to "sync"), "kv.durability"))
        assertEquals(Durability.GROUP, Rote.durabilityProp(props("kv.durability" to "GROUP"), "kv.durability"), "case-insensitive")
        assertEquals(Durability.GROUP, Rote.durabilityProp(props("kv.durability" to "group-commit"), "kv.durability"), "alias")
        assertEquals(Durability.ASYNC, Rote.durabilityProp(props("kv.durability" to " async  # write-behind"), "kv.durability"), "inline # comment")
        assertEquals(Durability.SYNC, Rote.durabilityProp(props("kv.durability" to "nope"), "kv.durability"), "unknown -> default")
        assertEquals(Durability.GROUP, Rote.durabilityProp(props("kv.durability" to "nope"), "kv.durability", Durability.GROUP), "unknown -> given default")
    }

    @Test
    fun `group commit coalesces concurrent writes and stays durable`() {
        val fake = FakeStore().apply { commitDelayMs = 30 }
        val store = GroupCommitStore(fake, Durability.GROUP, batchSize = 128, windowMs = 50)
        try {
            val n = 40
            val ready = CountDownLatch(n)
            val go = CountDownLatch(1)
            val notDurable = ConcurrentLinkedQueue<String>()
            val threads = (1..n).map { i ->
                Thread {
                    val k = "k$i"
                    store.put(k, "v$i")
                    ready.countDown()
                    go.await()
                    store.commit()
                    if (fake.durableValue(k) != "v$i") notDurable.add(k)
                }
            }
            threads.forEach { it.start() }
            ready.await()
            go.countDown()
            threads.forEach { it.join() }

            assertTrue(notDurable.isEmpty(), "every write must be durable once its commit() returned: $notDurable")
            assertEquals((1..n).count { fake.durableValue("k$it") == "v$it" }, n, "all writes persisted")
            assertTrue(fake.commits.get() <= 2, "40 concurrent commits must coalesce (was ${fake.commits.get()})")
        } finally {
            store.close()
        }
    }

    @Test
    fun `a failed batch commit is surfaced to the callers`() {
        val fake = FakeStore().apply { failCommit = true }
        val store = GroupCommitStore(fake, Durability.GROUP, windowMs = 1)
        try {
            store.put("k", "v")
            assertThrows(RuntimeException::class.java) { store.commit() }
        } finally {
            store.close()
        }
    }

    @Test
    fun `async commit returns before the write is flushed`() {
        val fake = FakeStore().apply { commitDelayMs = 400 }
        val store = GroupCommitStore(fake, Durability.ASYNC)
        try {
            store.put("a", "1")
            val start = System.nanoTime()
            store.commit()
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            assertTrue(elapsedMs < 200, "async commit must not wait for the flush (was ${elapsedMs}ms)")

            var flushed = false
            val deadline = System.currentTimeMillis() + 3_000
            while (System.currentTimeMillis() < deadline) {
                if (fake.durableValue("a") == "1") { flushed = true; break }
                Thread.sleep(10)
            }
            assertTrue(flushed, "async write must be flushed in the background")
        } finally {
            store.close()
        }
    }

    @Test
    fun `supportsGroupCommit is off unless a backend opts in`() {
        assertTrue(MVStorePlug().supportsGroupCommit, "MVStore opts in")
        val plain = object : KVStore {
            override fun init(vararg params: Any?) {}
            override fun put(key: String, value: String) {}
            override fun get(key: String): String? = null
            override fun delete(key: String) {}
            override fun commit() {}
            override fun close() {}
            override fun forEach(action: (String, String) -> Unit) {}
        }
        assertFalse(plain.supportsGroupCommit, "default is off")
    }

    @Test
    fun `factory wraps a supporting backend only when durability is not sync`() {
        val base = Files.createTempDirectory("xdb-group").toFile()
        File(base, "xy").mkdirs()
        val previous = onmindxdb.dbfile
        onmindxdb.dbfile = File(base, "xy/xybox.xdb").absolutePath
        try {
            val sync = KVStoreFactory.createStore(props("kv.store" to "mvstore"))
            assertTrue(sync is MVStorePlug, "sync keeps the raw backend")
            sync.close()

            val grouped = KVStoreFactory.createStore(
                props("kv.store" to "mvstore", "kv.durability" to "group")
            )
            assertTrue(grouped is GroupCommitStore, "group wraps a supporting backend")
            grouped.close()
        } finally {
            onmindxdb.dbfile = previous
            base.deleteRecursively()
        }
    }
}
