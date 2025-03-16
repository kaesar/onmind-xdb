package co.onmind.trait

interface KVStore {
    fun init(vararg params: Any?)

    fun put(key: String, value: String)

    fun get(key: String): String?

    fun delete(key: String)

    fun commit()

    fun close()
}
