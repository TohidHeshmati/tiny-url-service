package org.tohid.tinyurlservice.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.controller.dtos.ResolveResponseDTO
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.isExpired
import org.tohid.tinyurlservice.exception.NotFoundException
import org.tohid.tinyurlservice.repository.UrlRepository

@Service
class UrlResolverService(
    private val urlRepository: UrlRepository,
) {
    @Cacheable(cacheNames = ["short-urls"], key = "#shortUrl")
    fun resolve(shortUrl: String): ResolveResponseDTO {
        val url =
            urlRepository.findByShortUrl(shortUrl)
                ?: throw NotFoundException("Short URL not found: $shortUrl")

        if (url.isExpired()) {
            urlRepository.delete(url)
            throw NotFoundException("Short URL has expired: $shortUrl")
        }

        return ResolveResponseDTO(
            originalUrl = url.originalUrl,
            expiryDate = url.expiryDate,
        )
    }

    @Cacheable(cacheNames = ["original-urls"], key = "#originalUrl", unless = "#result == null")
    fun getByOriginalUrl(originalUrl: String): Url? = urlRepository.findByOriginalUrl(originalUrl)
}
