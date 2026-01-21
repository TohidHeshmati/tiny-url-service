package org.tohid.tinyurlservice.service

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.controller.dtos.DataPoint
import org.tohid.tinyurlservice.controller.dtos.Granularity
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenResponseDTO
import org.tohid.tinyurlservice.controller.dtos.TimeSeriesData
import org.tohid.tinyurlservice.controller.dtos.UrlStatsResponseDTO
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.toShortenResponseDTO
import org.tohid.tinyurlservice.exception.NotFoundException
import org.tohid.tinyurlservice.repository.UrlDailyClicksRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClicksRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.beans.factory.annotation.Value

@Service
class UrlService(
    private val urlRepository: UrlRepository,
    private val urlResolverService: UrlResolverService,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val analyticsService: UrlAnalyticsService,
    @Value("\${base-url}") private val baseUrl: String,
    private val urlDailyClicksRepository: UrlDailyClicksRepository,
    private val urlHourlyClicksRepository: UrlHourlyClicksRepository,
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

        analyticsService.incrementClickCount(url.id)

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
        fromDate: Instant,
        toDate: Instant,
    ): UrlStatsResponseDTO {
        val url = urlRepository.findByShortUrl(shortCode) ?: throw NotFoundException("Shortcode $shortCode not found")

        val dataPoints =
            when (granularity) {
                Granularity.DAY ->
                    urlDailyClicksRepository
                        .findAllByUrlIdAndClickDateBetween(
                            url.id,
                            fromDate.truncatedTo(ChronoUnit.DAYS).atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                            toDate.truncatedTo(ChronoUnit.DAYS).atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                        ).map { DataPoint(it.clickDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC), it.count) }

                Granularity.HOUR ->
                    urlHourlyClicksRepository
                        .findAllByUrlIdAndClickHourBetween(url.id, fromDate, toDate)
                        .map { DataPoint(it.clickHour, it.count) }
            }

        return UrlStatsResponseDTO(
            shortCode = url.shortUrl,
            longUrl = url.originalUrl,
            createdAt = url.createdAt,
            totalClicks = url.totalClickCount,
            timeSeries =
                TimeSeriesData(
                    granularity = granularity,
                    from = fromDate,
                    to = toDate,
                    dataPoints = dataPoints,
                ),
        )
    }
}
