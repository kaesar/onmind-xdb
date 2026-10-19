package co.onmind.kv

import co.onmind.trait.KVStore
import co.onmind.util.Durability
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Decorator that amortizes [KVStore.commit] across concurrent writers.
 *
 * Both backends that advertise [KVStore.supportsGroupCommit] stage a write in
 * memory and only touch their medium on `commit()`: MVStore buffers in the MVMap
 * and flushes the file; DynamoDB buffers in `pending` and sends one
 * `BatchWriteItem` (up to 25 items). So [put]/[delete] pass straight through and
 * only `commit()` is coalesced: concurrent callers are released by a **single**
 * delegate commit (one file flush / one batch request instead of N).
 *
 * - [Durability.GROUP]: `commit()` blocks until the batch that contains the
 *   caller's write is durable. Durability is unchanged (the ack still follows the
 *   flush); only the flush is shared. Each caller waits at most [windowMs].
 * - [Durability.ASYNC]: `commit()` returns immediately (write-behind). The
 *   response may be sent before the data reaches the medium, so RPO > 0 by design.
 *
 * The decorator is only built for a durable-policy other than `sync` **and** a
 * backend that supports grouping (see `KVStoreFactory`).
 */
class GroupCommitStore(
    private val delegate: KVStore,
    private val mode: Durability,
    private val batchSize: Int = 128,
    private val windowMs: Long = 5
) : KVStore {

    override val supportsGroupCommit: Boolean = true

    private val lock = ReentrantLock()
    private val dirty = lock.newCondition()
    private var pendingRequest = false
    private var closed = false
    private val waiters = ArrayList<CompletableFuture<Unit>>()

    private val committer = Thread({ commitLoop() }, "xdb-group-commit").apply {
        isDaemon = true
        start()
    }

    // ---- KVStore (writes stage in the delegate; only commit is coalesced) ----

    override fun init(vararg params: Any?) = delegate.init(*params)

    override fun put(key: String, value: String) = delegate.put(key, value)

    override fun get(key: String): String? = delegate.get(key)

    override fun delete(key: String) = delegate.delete(key)

    override fun commit() {
        if (mode == Durability.ASYNC) {
            signal()
            return
        }
        awaitCommit()
    }

    override fun forEach(action: (String, String) -> Unit) {
        // Reading back the whole store must see every staged write.
        awaitCommit()
        delegate.forEach(action)
    }

    override fun close() {
        try {
            awaitCommit()
        } catch (_: Exception) {
            // Best effort: still shut the committer down and close the delegate.
        }
        lock.lock()
        try {
            closed = true
            dirty.signalAll()
        } finally {
            lock.unlock()
        }
        committer.join(2000)
        delegate.close()
    }

    // ---- internals ----

    private fun signal() {
        lock.lock()
        try {
            pendingRequest = true
            dirty.signalAll()
        } finally {
            lock.unlock()
        }
    }

    /** Enqueues the caller and blocks until the batch that includes its write is committed. */
    private fun awaitCommit() {
        val future = CompletableFuture<Unit>()
        lock.lock()
        try {
            if (closed) {
                // Committer is gone: commit inline so the caller is never stuck.
                delegate.commit()
                return
            }
            waiters.add(future)
            pendingRequest = true
            dirty.signalAll()
        } finally {
            lock.unlock()
        }
        try {
            future.get()
        } catch (e: ExecutionException) {
            throw (e.cause as? Exception ?: IllegalStateException("commit failed", e))
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("interrupted while waiting for commit", e)
        }
    }

    private fun commitLoop() {
        while (true) {
            var batch: List<CompletableFuture<Unit>> = emptyList()
            var stop = false
            lock.lock()
            try {
                while (!pendingRequest && !closed) dirty.await()
                if (closed && !pendingRequest) {
                    stop = true
                } else {
                    // Linger a little so a burst of concurrent writers joins one batch.
                    val deadline = System.currentTimeMillis() + windowMs
                    while (pendingRequest && waiters.size < batchSize) {
                        val remaining = deadline - System.currentTimeMillis()
                        if (remaining <= 0) break
                        dirty.await(remaining, TimeUnit.MILLISECONDS)
                    }
                    pendingRequest = false
                    batch = ArrayList(waiters)
                    waiters.clear()
                }
            } catch (_: InterruptedException) {
                stop = true
            } finally {
                lock.unlock()
            }
            if (stop) return
            try {
                delegate.commit()
                batch.forEach { it.complete(Unit) }
            } catch (e: Throwable) {
                // Fail the whole batch: the caller must not get a false ack.
                batch.forEach { it.completeExceptionally(e) }
            }
        }
    }
}
