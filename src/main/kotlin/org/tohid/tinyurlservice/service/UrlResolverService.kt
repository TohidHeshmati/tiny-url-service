package org.tohid.tinyurlservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.controller.dtos.ResolveResponseDTO
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.isExpired
import org.tohid.tinyurlservice.exception.NotFoundException
import org.tohid.tinyurlservice.repository.UrlRepository
import java.time.Instant

private const val CLICK_STREAM_KEY = "click_events"

@Service
class UrlResolverService(
    private val urlRepository: UrlRepository,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private data class ClickEvent(
        val shortCode: String,
        val timestamp: Instant,
    )

    @Cacheable(cacheNames = ["short-urls"], key = "#shortUrl")
    fun resolveAndRecordClick(shortUrl: String): ResolveResponseDTO {
        val url =
            urlRepository.findByShortUrl(shortUrl)
                ?: throw NotFoundException("Short URL not found: $shortUrl")

        if (url.isExpired()) {
            urlRepository.delete(url)
            throw NotFoundException("Short URL has expired: $shortUrl")
        }

        val event = ClickEvent(shortCode = url.shortUrl, timestamp = Instant.now())
        val eventPayload = objectMapper.writeValueAsString(event)
        redisTemplate.opsForStream<String, String>().add(CLICK_STREAM_KEY, mapOf("payload" to eventPayload))

        return ResolveResponseDTO(
            originalUrl = url.originalUrl,
            expiryDate = url.expiryDate,
        )
    }

    @Cacheable(cacheNames = ["original-urls"], key = "#originalUrl", unless = "#result == null")
    fun getByOriginalUrl(originalUrl: String): Url? = urlRepository.findByOriginalUrl(originalUrl)
}
