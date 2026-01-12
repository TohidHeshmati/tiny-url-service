package org.tohid.tinyurlservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.UrlDailyClick
import org.tohid.tinyurlservice.domain.UrlHourlyClick
import java.time.Instant
import java.time.LocalDate

@Repository
interface UrlDailyClickRepository : JpaRepository<UrlDailyClick, Long> {
    fun findAllByUrlAndClickDateBetween(
        url: Url,
        start: LocalDate,
        end: LocalDate,
    ): List<UrlDailyClick>

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO url_daily_clicks (url_id, click_date, count, created_at, updated_at)
            VALUES (:url_id, :click_date, :count, NOW(), NOW())
            ON DUPLICATE KEY UPDATE count = count + :count, updated_at = NOW()
        """,
        nativeQuery = true,
    )
    fun upsert(
        @Param("url_id") urlId: Long,
        @Param("click_date") clickDate: LocalDate,
        @Param("count") count: Long,
    )
}

@Repository
interface UrlHourlyClickRepository : JpaRepository<UrlHourlyClick, Long> {
    fun findAllByUrlAndClickHourBetween(
        url: Url,
        start: Instant,
        end: Instant,
    ): List<UrlHourlyClick>

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO url_hourly_clicks (url_id, click_hour, count, created_at, updated_at)
            VALUES (:url_id, :click_hour, :count, NOW(), NOW())
            ON DUPLICATE KEY UPDATE count = count + :count, updated_at = NOW()
        """,
        nativeQuery = true,
    )
    fun upsert(
        @Param("url_id") urlId: Long,
        @Param("click_hour") clickHour: Instant,
        @Param("count") count: Long,
    )
}
