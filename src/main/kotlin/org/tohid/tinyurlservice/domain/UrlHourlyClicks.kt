package org.tohid.tinyurlservice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDateTime

@Entity
@Table(name = "url_hourly_clicks")
data class UrlHourlyClicks(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "url_id", nullable = false)
    val urlId: Long,
    @Column(name = "click_hour", nullable = false)
    val clickHour: LocalDateTime,
    @Column(name = "count", nullable = false)
    var count: Long = 0,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = now(),
)
