package org.tohid.tinyurlservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO

class UrlServiceIT : BaseIntegrationTest() {
    @Test
    fun `shortens the request and records to DB`() {
        val originalUrl = "https://example.com"
        val request = ShortenRequestDTO(originalUrl = originalUrl)
        assertThat(urlRepository.count() == 0L)

        val shortenResponse = urlService.shorten(request)

        assertThat(shortenResponse.shortenedUrl).isNotBlank
        assertThat(urlRepository.count() == 1L)
        val savedUrl = urlRepository.findByOriginalUrl(originalUrl)
        assert(savedUrl != null)
        assert(savedUrl!!.originalUrl == originalUrl)
    }
}
