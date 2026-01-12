package org.tohid.tinyurlservice.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenResponseDTO
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.repository.UrlDailyClickRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClickRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import kotlin.test.assertEquals

class UrlServiceTest {
    private val urlRepository: UrlRepository = mock()
    private val urlResolverService: UrlResolverService = mock()
    private val shortCodeGenerator: ShortCodeGenerator = mock()
    private val dailyClickRepository: UrlDailyClickRepository = mock()
    private val hourlyClickRepository: UrlHourlyClickRepository = mock()
    private val testBaseUrl = "http://tohid-test-env.com"
    private val urlService =
        UrlService(
            urlRepository = urlRepository,
            urlResolverService = urlResolverService,
            shortCodeGenerator = shortCodeGenerator,
            dailyClickRepository = dailyClickRepository,
            hourlyClickRepository = hourlyClickRepository,
            baseUrl = testBaseUrl,
        )

    @Test
    fun `generates new shortened URL if original does not exist`() {
        val originalLong = "https://example.com/hello"
        val request = ShortenRequestDTO(originalLong)
        val short = "abcdefg"
        val expectedShortened = "$testBaseUrl/abcdefg"

        whenever(urlRepository.findByOriginalUrl(originalLong)).thenReturn(null)
        whenever(shortCodeGenerator.generate()).thenReturn(short)
        whenever(urlRepository.save(any())).thenAnswer { it.arguments[0] as Url }

        val response = urlService.shorten(request)

        assertEquals(ShortenResponseDTO(shortenedUrl = expectedShortened), response)
    }
}
