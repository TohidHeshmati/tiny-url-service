package org.tohid.tinyurlservice.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Component
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.repository.UrlRepository

/**
 * Cached lookups of [Url] by short code and by original URL.
 *
 * Kept in its own bean so callers get the caching proxy: a @Cacheable method called from
 * inside the same class bypasses the proxy and is never cached.
 * Business rules such as expiry are checked by the callers, so they also apply on a cache hit.
 */
@Component
class UrlLookupCache(
    private val urlRepository: UrlRepository,
) {
    @Cacheable(cacheNames = [SHORT_URLS], key = "#shortUrl", unless = "#result == null")
    fun findByShortUrl(shortUrl: String): Url? = urlRepository.findByShortUrl(shortUrl)

    @Cacheable(cacheNames = [ORIGINAL_URLS], key = "#originalUrl", unless = "#result == null")
    fun findByOriginalUrl(originalUrl: String): Url? = urlRepository.findByOriginalUrl(originalUrl)

    @Caching(
        evict = [
            CacheEvict(cacheNames = [SHORT_URLS], key = "#url.shortUrl"),
            CacheEvict(cacheNames = [ORIGINAL_URLS], key = "#url.originalUrl"),
        ],
    )
    fun evict(url: Url) = Unit

    @Caching(
        evict = [
            CacheEvict(cacheNames = [SHORT_URLS], allEntries = true),
            CacheEvict(cacheNames = [ORIGINAL_URLS], allEntries = true),
        ],
    )
    fun evictAll() = Unit

    companion object {
        const val SHORT_URLS = "short-urls"
        const val ORIGINAL_URLS = "original-urls"
    }
}
