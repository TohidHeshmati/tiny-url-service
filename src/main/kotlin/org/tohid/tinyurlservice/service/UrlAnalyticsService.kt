package org.tohid.tinyurlservice.service

import nl.basjes.parse.useragent.UserAgent
import nl.basjes.parse.useragent.UserAgentAnalyzer
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.tohid.tinyurlservice.domain.DeviceType
import org.tohid.tinyurlservice.repository.UrlDailyClicksRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClicksRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class UrlAnalyticsService(
    private val urlRepository: UrlRepository,
    private val urlHourlyClicksRepository: UrlHourlyClicksRepository,
    private val urlDailyClicksRepository: UrlDailyClicksRepository,
) {
    private val uaa =
        UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withCache(1000)
            .build()

    @Async
    @Transactional
    fun incrementClickCount(
        urlId: Long,
        userAgent: String?,
    ) {
        val now = Instant.now()
        val today = LocalDate.ofInstant(now, ZoneOffset.UTC)
        val hourSlot = now.truncatedTo(ChronoUnit.HOURS)

        val deviceType = parseDeviceType(userAgent)

        urlRepository.incrementTotalClickCount(urlId)
        urlHourlyClicksRepository.incrementClick(urlId, hourSlot, deviceType.name)
        urlDailyClicksRepository.incrementClick(urlId, today, deviceType.name)
    }

    private fun parseDeviceType(userAgent: String?): DeviceType {
        if (userAgent.isNullOrBlank()) return DeviceType.OTHER
        val agent = uaa.parse(userAgent)
        val deviceClass = agent.getValue(UserAgent.DEVICE_CLASS)

        return when (deviceClass) {
            "Mobile" -> DeviceType.MOBILE
            "Tablet" -> DeviceType.TABLET
            "Desktop" -> DeviceType.DESKTOP
            "Phone" -> DeviceType.MOBILE
            else -> DeviceType.OTHER
        }
    }
}
