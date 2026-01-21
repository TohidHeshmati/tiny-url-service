package org.tohid.tinyurlservice.controller.dtos

import java.io.Serializable
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit

data class ShortenResponseDTO(
    val shortenedUrl: String,
    val expiryDate: Instant? = null,
)

data class ResolveResponseDTO(
    val originalUrl: String,
    val expiryDate: Instant? = null,
    val totalClickCount: Long = 0,
) : Serializable

data class ErrorResponseDTO(
    val error: String,
    val time: Instant = now().truncatedTo(ChronoUnit.SECONDS),
) : Serializable

enum class Granularity {
    DAY,
    HOUR,
}

data class UrlStatsResponseDTO(
    val shortCode: String,
    val longUrl: String,
    val createdAt: Instant,
    val totalClicks: Long,
    val timeSeries: TimeSeriesData,
)

data class TimeSeriesData(
    val granularity: Granularity,
    val from: Instant,
    val to: Instant,
    val dataPoints: List<DataPoint>,
)

data class DataPoint(
    val timestamp: Instant,
    val clicks: Long,
)
