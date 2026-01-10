package org.tohid.tinyurlservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.repository.UrlRepository
import org.tohid.tinyurlservice.service.ShortCodeGenerator
import org.tohid.tinyurlservice.service.UrlResolverService
import org.tohid.tinyurlservice.service.UrlService
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var urlRepository: UrlRepository

    @Autowired
    protected lateinit var urlService: UrlService

    @Autowired
    protected lateinit var urlResolverService: UrlResolverService

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var shortCodeGenerator: ShortCodeGenerator

    @Autowired
    protected lateinit var redisTemplate: StringRedisTemplate

    @LocalServerPort
    private var port: Int = 0

    protected lateinit var baseUrl: String
    protected lateinit var shortenEndpoint: String
    protected lateinit var resolveEndpoint: String

    @BeforeEach
    fun cleanup() {
        baseUrl = "http://localhost:$port"
        shortenEndpoint = "http://localhost:$port/api/v1/urls"
        resolveEndpoint = "http://localhost:$port/api/v1/urls"
        println("Base URL for tests: $baseUrl")
        urlRepository.deleteAll()
        redisTemplate.connectionFactory?.connection?.flushDb()
    }

    val headers: HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

    val redirectSafeRestTemplate =
        RestTemplate(
            HttpComponentsClientHttpRequestFactory().apply {
                httpClient = HttpClients.custom().disableRedirectHandling().build()
            },
        )

    protected fun makeUrl(
        originalUrl: String = "https://example.com",
        shortUrl: String? = null,
        expiryDate: Instant? = null,
    ) = Url(
        originalUrl = originalUrl,
        shortUrl = shortUrl ?: shortCodeGenerator.generate(),
        expiryDate = expiryDate,
    )
}
