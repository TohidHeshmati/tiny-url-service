package org.tohid.tinyurlservice.controller.dtos

import java.time.Instant

data class UrlStatsResponse(
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

data class SystemSummaryResponse(
    val totalLinks: Long,
    val totalClicksAllTime: Long,
    val topLinks: List<TopLink>,
)

data class TopLink(
    val shortCode: String,
    val totalClicks: Long,
)

enum class Granularity {
    HOUR,
    DAY,
}
