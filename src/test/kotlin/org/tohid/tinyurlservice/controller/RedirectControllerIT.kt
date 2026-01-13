package org.tohid.tinyurlservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.tohid.tinyurlservice.BaseIntegrationTest
import java.net.URI
import java.time.Instant.now

class RedirectControllerIT : BaseIntegrationTest() {
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
}
