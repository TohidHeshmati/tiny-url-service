package org.tohid.tinyurlservice.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.domain.Url
import java.time.Instant.now

class UrlRepositoryIT : BaseIntegrationTest() {
    private val originalBase = "http://example.com/something/long"

    @Test
    fun `findByShortUrl returns correct Url if exists`() {
        val shortUrlToBeFound = shortCodeGenerator.generate()
        val shortUrlNotToBeFound = shortCodeGenerator.generate()
        urlRepository.saveAll(
            listOf(
                makeUrl(originalUrl = "$originalBase/1", shortUrl = shortUrlToBeFound),
                makeUrl(originalUrl = "$originalBase/2", shortUrl = shortUrlNotToBeFound),
            ),
        )

        val found = urlRepository.findByShortUrl(shortUrlToBeFound)

        assertThat(found).isNotNull
        assertThat(found!!.shortUrl).isEqualTo(shortUrlToBeFound)
        assertThat(found.shortUrl).isNotEqualTo(shortUrlNotToBeFound)
    }

    @Test
    fun `findByOriginalUrl returns correct Url if exists`() {
        val expectedShort = shortCodeGenerator.generate()
        val expectedLong = "$originalBase/$expectedShort"
        val notExpectedShort = shortCodeGenerator.generate()
        val notExpectedLong = "$originalBase/$notExpectedShort"
        urlRepository.saveAll(
            listOf(
                makeUrl(
                    shortUrl = expectedShort,
                    originalUrl = expectedLong,
                ),
                makeUrl(
                    shortUrl = notExpectedShort,
                    originalUrl = notExpectedLong,
                ),
            ),
        )

        val found = urlRepository.findByOriginalUrl(expectedLong)

        assertThat(found).isNotNull
        assertThat(found!!.shortUrl).isEqualTo(expectedShort)
        assertThat(found.originalUrl).isEqualTo(expectedLong)
        assertThat(found.originalUrl).isNotEqualTo(notExpectedShort)
    }

    @Test
    fun `deletes expired urls`() {
        val expiredUrl =
            urlRepository.save(
                Url(
                    shortUrl = "expired",
                    originalUrl = "http://example.com/expired",
                    expiryDate = now().minusSeconds(3600),
                ),
            )
        val nonExpiredUrl =
            urlRepository.save(
                Url(
                    shortUrl = "non-expired",
                    originalUrl = "http://example.com/non-expired",
                    expiryDate = now().plusSeconds(3600),
                ),
            )

        val deletedCount = urlRepository.deleteByExpiryDateBefore(time = now())

        assertEquals(1, deletedCount)
        assertThat(urlRepository.findByShortUrl(expiredUrl.shortUrl)).isNull()
        assertThat(urlRepository.findByShortUrl(nonExpiredUrl.shortUrl)).isNotNull
    }
}
