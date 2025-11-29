package co.onmind.kv

import co.onmind.trait.KVStore
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class MVStorePlug: KVStore {

    private var mvStore: MVStore? = null
    private var mvMap: MVMap<String, String>? = null

    override fun init(vararg params: Any?) {
        val fileName = params[0] as String?
        val store =  params[1] as String?
        mvStore = MVStore.open(fileName)
        mvMap = mvStore?.openMap(store)
    }

    fun map(): MVMap<String, String>? {
        return mvMap
    }

    override fun put(key: String, value: String) {
        mvMap?.put(key, value)
    }

    override fun get(key: String): String? {
        return mvMap?.get(key)
    }

    override fun delete(key: String) {
        mvMap?.remove(key)
    }

    override fun commit() {
        mvStore?.commit()
    }

    override fun close() {
        mvStore?.close()
    }
    
    override fun forEach(action: (String, String) -> Unit) {
        mvMap?.forEach { (key, value) -> action(key, value) }
    }
}
