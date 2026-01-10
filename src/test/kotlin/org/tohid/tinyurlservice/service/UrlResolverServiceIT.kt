package org.tohid.tinyurlservice.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.exception.NotFoundException
import java.time.Instant
import kotlin.test.assertEquals

class UrlResolverServiceIT : BaseIntegrationTest() {
    @Test
    fun `resolves Url from cache if it exists and cache is hit`() {
        val originalUrl = "https://example.com"
        val expectedShortUrl = shortCodeGenerator.generate()
        urlRepository.save(
            Url(
                originalUrl = originalUrl,
                shortUrl = expectedShortUrl,
            ),
        )

        val firsResolveFromDB = urlResolverService.resolve(expectedShortUrl)
        assert(firsResolveFromDB.originalUrl == originalUrl)

        urlRepository.deleteAll()
        assert(urlRepository.count() == 0L)

        val secondResolveFromCache = urlResolverService.resolve(expectedShortUrl)
        assert(secondResolveFromCache.originalUrl == originalUrl)
    }

    @Test
    fun `resolved expired Url is deleted from cache and db`() {
        val originalUrl = "https://expired.com"
        val shortUrl = shortCodeGenerator.generate()
        urlRepository.save(
            Url(
                originalUrl = originalUrl,
                shortUrl = shortUrl,
                expiryDate = Instant.now().minusSeconds(60), // 1 min ago
            ),
        )

        val exception =
            assertThrows<NotFoundException> {
                urlResolverService.resolve(shortUrl)
            }
        assertEquals("Short URL has expired: $shortUrl", exception.message)

        assert(urlRepository.count() == 0L)
        val cached = redisTemplate.opsForValue().get("short:$shortUrl")
        assert(cached == null)
    }

    @Test
    fun `returns Url and caches result from redis cache`() {
        val originalUrl = "https://example.com/original"
        val shortUrl = "abc123"
        urlRepository.save(Url(originalUrl = originalUrl, shortUrl = shortUrl))

        val firstCall = urlResolverService.getByOriginalUrl(originalUrl)
        assertNotNull(firstCall)
        assertEquals(originalUrl, firstCall.originalUrl)

        urlRepository.deleteAll()
        assertEquals(0L, urlRepository.count())

        val secondCall = urlResolverService.getByOriginalUrl(originalUrl)
        assertNotNull(secondCall)
        assertEquals(originalUrl, secondCall.originalUrl)
    }

    @Test
    fun `returns null and does not cache when originalUrl does not exist`() {
        val missingUrl = "https://does-not-exist.com"

        val result = urlResolverService.getByOriginalUrl(missingUrl)
        assertNull(result)

        val cached = redisTemplate.opsForValue().get("original-urls::$missingUrl")
        assertNull(cached)
    }

    @Test
    fun `caches only successful lookups`() {
        val url = Url(originalUrl = "https://cached.com", shortUrl = "shortyy")
        urlRepository.save(url)

        // First call caches it
        val fromDb = urlResolverService.getByOriginalUrl(url.originalUrl)
        assertNotNull(fromDb)

        // Ensure Redis contains it
        val redisKey = "original-urls::${url.originalUrl}"
        val cached = redisTemplate.opsForValue().get(redisKey)
        assertNotNull(cached)
    }

    @Test
    fun `null values are not put in cache`() {
        val nonExistent = "https://not-in-db.com"
        val first = urlResolverService.getByOriginalUrl(nonExistent)
        assertNull(first)

        val redisKey = "original-urls::$nonExistent"
        val redisValue = redisTemplate.opsForValue().get(redisKey)
        assertNull(redisValue)
    }
}
