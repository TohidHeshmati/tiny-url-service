package org.tohid.tinyurlservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.repository.UrlHourlyClicksRepository
import java.net.URI
import java.time.Instant.now

class RedirectControllerIT : BaseIntegrationTest() {
    @Autowired
    lateinit var urlHourlyClicksRepository: UrlHourlyClicksRepository

    @Test
    fun `redirects permanently status=301 for shortened URL without expiry date`() {
        val savedUrl = urlRepository.save(makeUrl(expiryDate = null))

        val response: ResponseEntity<String> =
            redirectSafeRestTemplate.exchange(
                "$baseUrl/${savedUrl.shortUrl}",
                HttpMethod.GET,
                null,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY)
        assertThat(response.headers.location).isEqualTo(URI.create(savedUrl.originalUrl))
    }

    @Test
    fun `redirects temporary status=302 for shortened URL with expiry date in future`() {
        val savedUrl = urlRepository.save(makeUrl(expiryDate = now().plusSeconds(3600)))

        val response: ResponseEntity<String> =
            redirectSafeRestTemplate.exchange(
                "$baseUrl/${savedUrl.shortUrl}",
                HttpMethod.GET,
                null,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location).isEqualTo(URI.create(savedUrl.originalUrl))
    }

    @Test
    fun `redirect records device type based on User-Agent`() {
        val savedUrl = urlRepository.save(makeUrl())
        val headers = org.springframework.http.HttpHeaders()
        headers.add(
            "User-Agent",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
        )
        val entity = org.springframework.http.HttpEntity<String>(headers)

        redirectSafeRestTemplate.exchange(
            "$baseUrl/${savedUrl.shortUrl}",
            HttpMethod.GET,
            entity,
            String::class.java,
        )

        // Wait for async execution
        org.awaitility.Awaitility.await().untilAsserted {
            val hourlyClicks =
                urlHourlyClicksRepository.findAllByUrlIdAndClickHourBetween(
                    savedUrl.id,
                    now().minusSeconds(3600),
                    now().plusSeconds(3600),
                )
            assertThat(hourlyClicks).isNotEmpty
            assertThat(hourlyClicks[0].deviceType).isEqualTo(org.tohid.tinyurlservice.domain.DeviceType.MOBILE)
        }
    }
}
