package co.onmind.kv

import co.onmind.trait.KVStore
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.expiry.Duration
import org.ehcache.expiry.Expirations
import java.util.concurrent.TimeUnit

class EhCachePlug: KVStore {

    private var cache: Cache<String, String>? = null

    override fun init(vararg params: Any?) {
        val store =  params[1] as String?
        val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build()
        cacheManager.init()
        this.cache = cacheManager.createCache(
            store,
            CacheConfigurationBuilder.newCacheConfigurationBuilder<String, String>(
                String::class.java, String::class.java,
                ResourcePoolsBuilder.heap(100)
            )
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(1, TimeUnit.HOURS)))
                .build()
        )
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

    override fun commit() {}

    override fun close() {}
}
