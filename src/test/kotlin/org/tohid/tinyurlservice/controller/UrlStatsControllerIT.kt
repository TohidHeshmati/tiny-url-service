package org.tohid.tinyurlservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.controller.dtos.UrlStatsResponseDTO

class UrlStatsControllerIT : BaseIntegrationTest() {
    @Test
    fun `returns stats with default range when dates are not provided for hourly granularity`() {
        val savedUrl = urlRepository.save(makeUrl())

        val response: ResponseEntity<UrlStatsResponseDTO> =
            restTemplate.exchange(
                "$baseUrl/api/v1/urls/${savedUrl.shortUrl}/stats?granularity=hour",
                HttpMethod.GET,
                null,
                UrlStatsResponseDTO::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
    }

    @Test
    fun `returns stats with default range when dates are not provided for daily granularity`() {
        val savedUrl = urlRepository.save(makeUrl())

        val response: ResponseEntity<UrlStatsResponseDTO> =
            restTemplate.exchange(
                "$baseUrl/api/v1/urls/${savedUrl.shortUrl}/stats?granularity=DAY",
                HttpMethod.GET,
                null,
                UrlStatsResponseDTO::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
    }
}
