package org.tohid.tinyurlservice.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.controller.dtos.ErrorResponseDTO
import org.tohid.tinyurlservice.controller.dtos.ResolveResponseDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenResponseDTO
import java.time.Instant.now

class UrlApiControllerIT : BaseIntegrationTest() {
    @Test
    fun `shortens a valid URL`() {
        val entity =
            HttpEntity(
                ShortenRequestDTO(originalUrl = "https://www.example.com"),
                headers,
            )

        val response: ResponseEntity<ShortenResponseDTO> =
            restTemplate.postForEntity(
                shortenEndpoint,
                entity,
                ShortenResponseDTO::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        assertThat(response.body!!.shortenedUrl).isNotBlank
    }

    @Test
    fun `resolves shortened URL`() {
        val savedUrl = urlRepository.save(makeUrl())

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                "$resolveEndpoint/${savedUrl.shortUrl}",
                HttpMethod.GET,
                null,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)

        val body = objectMapper.readValue(response.body, ResolveResponseDTO::class.java)
        assertThat(body.originalUrl).isEqualTo(savedUrl.originalUrl)
    }

    @Test
    fun `returns not found for shortened URL with expiry date in past`() {
        val savedUrl = urlRepository.save(makeUrl(expiryDate = now().minusSeconds(3600)))

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                "$baseUrl/${savedUrl.shortUrl}",
                HttpMethod.GET,
                null,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        val body = objectMapper.readValue(response.body, ErrorResponseDTO::class.java)
        assertThat(body.time).isBetween(now().minusSeconds(60), now())
        assertThat(body.error).isEqualTo("Short URL has expired: ${savedUrl.shortUrl}")
    }

    @Test
    fun `removes the url from db when requested and expiry date in past`() {
        val savedUrl = urlRepository.save(makeUrl(expiryDate = now().minusSeconds(3600)))

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                "$baseUrl/${savedUrl.shortUrl}",
                HttpMethod.GET,
                null,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        val body = objectMapper.readValue(response.body, ErrorResponseDTO::class.java)
        assertThat(body.time).isBetween(now().minusSeconds(60), now())
        assertThat(body.error).isEqualTo("Short URL has expired: ${savedUrl.shortUrl}")
        assertThat(urlRepository.findByShortUrl(savedUrl.shortUrl)).isNull()
    }

    @Test
    fun `returns 404 for non-existent short URL`() {
        val response = restTemplate.getForEntity("$baseUrl/unknown123", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val error = objectMapper.readTree(response.body)
        assertThat(error["error"].asText()).contains("Short URL not found")
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `returns 400 for blank URL input`() {
            val request = ShortenRequestDTO(originalUrl = "")

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ErrorResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ErrorResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            val body = response.body
            assertThat(body?.error).isEqualTo("originalUrl: URL must not be blank")
            assertThat(body?.time).isBetween(now().minusSeconds(60), now())
        }

        @Test
        fun `returns 400 when originalUrl exceeds 512 characters`() {
            val longUrl = "http://example.com/" + "x".repeat(500)
            val request = ShortenRequestDTO(originalUrl = longUrl)

            val entity = HttpEntity(request, headers)
            val response = restTemplate.postForEntity(shortenEndpoint, entity, ErrorResponseDTO::class.java)

            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `returns 400 for invalid URL input`() {
            val request = ShortenRequestDTO(originalUrl = "not_a_url")

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ErrorResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ErrorResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            val body = response.body
            assertThat(body?.time).isBetween(now().minusSeconds(60), now())
            assertThat(body?.error).isEqualTo("originalUrl: Must be a valid URL")
        }

        @ParameterizedTest
        @ValueSource(
            strings =
                [
                    "2000-01-01T00:00:00Z",
                    "2023-10-01T00:00:00Z",
                ],
        )
        fun `returns 400 for expired expiryDate`(timeInPast: String) {
            val request =
                mapOf(
                    "original_url" to "https://example.com",
                    "expiry_date" to timeInPast,
                )

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ErrorResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ErrorResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.body?.error).contains("expiryDate: The date must be in the future")
        }

        @Test
        fun `returns 201 for valid future expiryDate`() {
            val request =
                mapOf(
                    "original_url" to "https://example.com",
                    "expiry_date" to now().plusSeconds(3600).toString(),
                )

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ShortenResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ShortenResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(response.body?.shortenedUrl).isNotNull()
        }

        @Test
        fun `returns 201 for valid deep in future expiryDate`() {
            val request =
                mapOf(
                    "original_url" to "https://example.com",
                    "expiry_date" to "2999-12-31T23:59:59Z",
                )

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ShortenResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ShortenResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(response.body?.shortenedUrl).isNotNull()
        }

        @ParameterizedTest
        @ValueSource(
            strings =
                [
                    "not-a-date",
                    "2023-01-01",
                    "2023-01-01 12:00:00",
                    "01-01-2023T12:00:00Z",
                ],
        )
        fun `returns 400 for malformed expiryDate formats`(invalidDate: String) {
            val request =
                mapOf(
                    "original_url" to "https://example.com",
                    "expiry_date" to invalidDate,
                )

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ErrorResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ErrorResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(
                response.body?.error,
            ).contains("expiry_date: Invalid date format for expiryDate. Must be ISO-8601.")
        }

        @Test
        fun `returns 400 for empty expiryDate`() {
            val request =
                mapOf(
                    "original_url" to "https://example.com",
                    "expiry_date" to "",
                )

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ErrorResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ErrorResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(
                response.body?.error,
            ).contains("expiry_date: Invalid date format for expiryDate. Must be ISO-8601.")
        }

        @Test
        fun `returns 201 if expiryDate is omitted`() {
            val request =
                mapOf(
                    "original_url" to "https://example.com",
                )

            val entity = HttpEntity(request, headers)
            val response: ResponseEntity<ShortenResponseDTO> =
                restTemplate.postForEntity(
                    shortenEndpoint,
                    entity,
                    ShortenResponseDTO::class.java,
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(response.body?.shortenedUrl).isNotNull()
        }
    }
}
