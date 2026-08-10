package com.mycodecalendar.backend.database

import java.util.concurrent.ConcurrentHashMap

object RedisCache {
    private val memoryCache = ConcurrentHashMap<String, Pair<String, Long>>()

    fun get(key: String): String? {
        val entry = memoryCache[key] ?: return null
        if (System.currentTimeMillis() > entry.second) {
            memoryCache.remove(key)
            return null
        }
        return entry.first
    }

    fun set(key: String, value: String, ttlSeconds: Long) {
        val expireAt = System.currentTimeMillis() + (ttlSeconds * 1000)
        memoryCache[key] = Pair(value, expireAt)
    }

    fun invalidate(prefix: String) {
        memoryCache.keys.removeIf { it.startsWith(prefix) }
    }
}
