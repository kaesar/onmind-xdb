package co.onmind.kv

import co.onmind.trait.KVStore
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class EhCachePlug: KVStore {

    private var cacheManager: CacheManager? = null
    private var cache: org.ehcache.Cache<String, String>? = null

    override fun init(vararg params: Any?) {
        val cacheName = params[0] as String? ?: "xybox"
        val maxEntries = (params[1] as? String)?.toLongOrNull() ?: 10000L

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .withCache(cacheName,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    String::class.java, String::class.java,
                    ResourcePoolsBuilder.heap(maxEntries)
                )
            )
            .build()

        cacheManager?.init()
        cache = cacheManager?.getCache(cacheName, String::class.java, String::class.java)
    }

    override fun put(key: String, value: String) {
        cache?.put(key, value)
    }

    override fun get(key: String): String? {
        return cache?.get(key)
    }

    override fun delete(key: String) {
        cache?.remove(key)
    }

    override fun commit() {
        // EhCache no requiere commit explícito
    }

    override fun close() {
        cacheManager?.close()
    }
    
    override fun forEach(action: (String, String) -> Unit) {
        cache?.forEach { entry -> action(entry.key, entry.value) }
    }
}