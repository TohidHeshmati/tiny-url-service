package org.tohid.tinyurlservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenResponseDTO
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.toShortenResponseDTO
import org.tohid.tinyurlservice.repository.UrlRepository
import java.net.URI

@Service
class UrlService(
    private val urlRepository: UrlRepository,
    private val urlResolverService: UrlResolverService,
    private val shortCodeGenerator: ShortCodeGenerator,
    @Value("\${base-url}") private val baseUrl: String,
) {
    fun shorten(request: ShortenRequestDTO): ShortenResponseDTO {
        val existing = urlResolverService.getByOriginalUrl(request.originalUrl)
        if (existing != null) return existing.toShortenResponseDTO(baseUrl)

        val shortUrl = shortCodeGenerator.generate()
        val url =
            urlRepository.save(
                Url(
                    originalUrl = request.originalUrl,
                    shortUrl = shortUrl,
                    expiryDate = request.expiryDate,
                ),
            )
        return url.toShortenResponseDTO(baseUrl)
    }

    fun redirectsByShortUrl(shortUrl: String): ResponseEntity<Void> {
        val url = urlResolverService.resolve(shortUrl)
        val location = URI.create(url.originalUrl)
        val status =
            if (url.expiryDate != null) {
                HttpStatus.FOUND
            } else {
                HttpStatus.MOVED_PERMANENTLY
            }

        return ResponseEntity.status(status).location(location).build()
    }
}
