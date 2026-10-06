package org.tohid.tinyurlservice.service

import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.isExpired
import org.tohid.tinyurlservice.exception.NotFoundException
import org.tohid.tinyurlservice.repository.UrlRepository

@Service
class UrlResolverService(
    private val urlRepository: UrlRepository,
    private val urlLookupCache: UrlLookupCache,
) {
    fun resolve(shortUrl: String): Url {
        val url =
            urlLookupCache.findByShortUrl(shortUrl)
                ?: throw NotFoundException("Short URL not found: $shortUrl")

        // Checked outside the cache so an expired link stops resolving even while it is still cached.
        if (url.isExpired()) {
            urlRepository.delete(url)
            urlLookupCache.evict(url)
            throw NotFoundException("Short URL has expired: $shortUrl")
        }

        return url
    }

    fun getByOriginalUrl(originalUrl: String): Url? = urlLookupCache.findByOriginalUrl(originalUrl)
}
