package co.onmind.kv

import co.onmind.trait.KVStore
/*
import org.rocksdb.ColumnFamilyDescriptor
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.Options
import org.rocksdb.RocksDB
import org.rocksdb.RocksIterator

class RocksDBPlug : KVStore {

    private var db: RocksDB? = null
    private var cfHandle: ColumnFamilyHandle? = null

    override fun init(vararg params: Any?) {
        val path = params[0] as String? ?: throw IllegalArgumentException("Path required")

        val options = Options()
            .setCreateIfMissing(true)
            .setCreateMissingColumnFamilies(true)

        db = RocksDB.open(options, path)
        val cfDescriptor = ColumnFamilyDescriptor("default".toByteArray())
        cfHandle = db?.createColumnFamily(cfDescriptor)
    }

    override fun put(key: String, value: String) {
        db?.put(cfHandle, key.toByteArray(), value.toByteArray())
    }

    override fun get(key: String): String? {
        val result = db?.get(cfHandle, key.toByteArray())
        return result?.decodeToString()
    }

    override fun delete(key: String) {
        db?.delete(cfHandle, key.toByteArray())
    }

    override fun commit() {
    }

    override fun close() {
        cfHandle?.close()
        db?.close()
    }

    override fun forEach(action: (String, String) -> Unit) {
        val iterator = db?.newIterator(cfHandle) ?: return
        iterator.seekToFirst()
        while (iterator.isValid) {
            val key = iterator.key().decodeToString()
            val value = iterator.value().decodeToString()
            action(key, value)
            iterator.next()
        }
        iterator.close()
    }
}
*/
