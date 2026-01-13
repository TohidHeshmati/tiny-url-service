package org.tohid.tinyurlservice.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.tohid.tinyurlservice.domain.UrlHourlyClicks
import java.time.LocalDateTime

@Repository
interface UrlHourlyClicksRepository : CrudRepository<UrlHourlyClicks, Long> {
    @Modifying
    @Transactional
    @Query(
        value = """
        INSERT INTO url_hourly_clicks (url_id, click_hour, count, created_at, updated_at)
        VALUES (:urlId, :hour, 1, NOW(), NOW())
        ON DUPLICATE KEY UPDATE count = count + 1, updated_at = NOW()
    """,
        nativeQuery = true,
    )
    fun incrementClick(
        urlId: Long,
        hour: LocalDateTime,
    )
}
