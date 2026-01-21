package org.tohid.tinyurlservice.controller.dtos

import java.time.Instant

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
    val deviceClicks: Map<String, Long> = emptyMap(),
)
