package co.onmind.trait

interface KVStore {
    /**
     * True when this backend stages a write in memory and only touches its
     * medium on [commit], so concurrent commits can be coalesced into one
     * flush (see `kv.durability` / `GroupCommitStore`). Default: false.
     */
    val supportsGroupCommit: Boolean get() = false

    fun init(vararg params: Any?)

    fun put(key: String, value: String)

    fun get(key: String): String?

    fun delete(key: String)

    fun commit()

    fun close()
    
    fun forEach(action: (String, String) -> Unit)
}
