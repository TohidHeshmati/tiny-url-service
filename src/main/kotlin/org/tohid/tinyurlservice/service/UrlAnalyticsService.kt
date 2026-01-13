package org.tohid.tinyurlservice.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.tohid.tinyurlservice.repository.UrlDailyClicksRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClicksRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class UrlAnalyticsService(
    private val urlRepository: UrlRepository,
    private val urlHourlyClicksRepository: UrlHourlyClicksRepository,
    private val urlDailyClicksRepository: UrlDailyClicksRepository,
) {
    @Async
    @Transactional
    fun incrementClickCount(urlId: Long) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val today = now.toLocalDate()
        val hourSlot = now.truncatedTo(ChronoUnit.HOURS)
        urlRepository.incrementTotalClickCount(urlId)
        urlHourlyClicksRepository.incrementClick(urlId, hourSlot)
        urlDailyClicksRepository.incrementClick(urlId, today)
    }
}
