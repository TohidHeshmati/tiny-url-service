package org.tohid.tinyurlservice.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.tohid.tinyurlservice.domain.UrlDailyClicks
import java.time.LocalDate

@Repository
interface UrlDailyClicksRepository : CrudRepository<UrlDailyClicks, Long> {
    fun findAllByUrlIdAndClickDateBetween(
        urlId: Long,
        start: LocalDate,
        end: LocalDate,
    ): List<UrlDailyClicks>

    @Modifying
    @Transactional
    @Query(
        value = """
        INSERT INTO url_daily_clicks (url_id, click_date, device_type, count, created_at, updated_at)
        VALUES (:urlId, :date, :deviceType, 1, NOW(), NOW())
        ON DUPLICATE KEY UPDATE count = count + 1, updated_at = NOW()
    """,
        nativeQuery = true,
    )
    fun incrementClick(
        urlId: Long,
        date: LocalDate,
        deviceType: String,
    )
}
