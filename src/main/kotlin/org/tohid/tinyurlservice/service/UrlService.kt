package org.tohid.tinyurlservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.controller.dtos.DataPoint
import org.tohid.tinyurlservice.controller.dtos.Granularity
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenResponseDTO
import org.tohid.tinyurlservice.controller.dtos.SystemSummaryResponse
import org.tohid.tinyurlservice.controller.dtos.TimeSeriesData
import org.tohid.tinyurlservice.controller.dtos.TopLink
import org.tohid.tinyurlservice.controller.dtos.UrlResponse
import org.tohid.tinyurlservice.controller.dtos.UrlStatsResponse
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.toShortenResponseDTO
import org.tohid.tinyurlservice.exception.NotFoundException
import org.tohid.tinyurlservice.repository.UrlDailyClickRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClickRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UrlService(
    private val urlRepository: UrlRepository,
    private val urlResolverService: UrlResolverService,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val dailyClickRepository: UrlDailyClickRepository,
    private val hourlyClickRepository: UrlHourlyClickRepository,
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
        val url = urlResolverService.resolveAndRecordClick(shortUrl)
        val location = URI.create(url.originalUrl)
        val status =
            if (url.expiryDate != null) {
                HttpStatus.FOUND
            } else {
                HttpStatus.MOVED_PERMANENTLY
            }

        return ResponseEntity.status(status).location(location).build()
    }

    fun getStatsForUrl(
        shortCode: String,
        granularity: Granularity,
        from: Instant,
        to: Instant,
    ): UrlStatsResponse {
        val url =
            urlRepository.findByShortUrl(shortCode)
                ?: throw NotFoundException("Short URL not found: $shortCode")

        val dataPoints =
            when (granularity) {
                Granularity.DAY ->
                    dailyClickRepository
                        .findAllByUrlAndClickDateBetween(
                            url,
                            from.truncatedTo(ChronoUnit.DAYS).atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                            to.truncatedTo(ChronoUnit.DAYS).atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                        ).map { DataPoint(it.clickDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC), it.count) }

                Granularity.HOUR ->
                    hourlyClickRepository
                        .findAllByUrlAndClickHourBetween(url, from, to)
                        .map { DataPoint(it.clickHour, it.count) }
            }

        return UrlStatsResponse(
            shortCode = url.shortUrl,
            longUrl = url.originalUrl,
            createdAt = url.createdAt,
            totalClicks = url.totalClickCount,
            timeSeries =
                TimeSeriesData(
                    granularity = granularity,
                    from = from,
                    to = to,
                    dataPoints = dataPoints,
                ),
        )
    }

    fun getSystemSummary(): SystemSummaryResponse {
        val totalLinks = urlRepository.count()
        val totalClicks = urlRepository.findAll().sumOf { it.totalClickCount }
        val topLinks =
            urlRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "totalClickCount")))
                .map { TopLink(it.shortUrl, it.totalClickCount) }
                .toList()

        return SystemSummaryResponse(
            totalLinks = totalLinks,
            totalClicksAllTime = totalClicks,
            topLinks = topLinks,
        )
    }

    fun getAllUrls(pageable: Pageable): Page<UrlResponse> =
        urlRepository.findAll(pageable).map {
            UrlResponse(
                shortCode = it.shortUrl,
                originalUrl = it.originalUrl,
                createdAt = it.createdAt,
                totalClickCount = it.totalClickCount,
            )
        }
}
