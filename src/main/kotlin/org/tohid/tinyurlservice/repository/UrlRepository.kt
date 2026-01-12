package org.tohid.tinyurlservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.tohid.tinyurlservice.domain.Url
import java.time.Instant

@Repository
interface UrlRepository : JpaRepository<Url, Long> {
    fun findByShortUrl(shortUrl: String): Url?

    fun findByOriginalUrl(originalUrl: String): Url?

    @Transactional
    @Modifying
    @Query(
        "DELETE FROM Url u WHERE u.expiryDate IS NOT NULL AND u.expiryDate < :time",
    )
    fun deleteByExpiryDateBefore(time: Instant): Int

    @Transactional
    @Modifying
    @Query("UPDATE Url u SET u.totalClickCount = u.totalClickCount + :count WHERE u.shortUrl = :shortUrl")
    fun incrementTotalClickCount(
        shortUrl: String,
        count: Long,
    )
}
