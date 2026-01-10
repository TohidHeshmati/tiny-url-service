package org.tohid.tinyurlservice.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.repository.UrlRepository
import java.time.Instant

@Service
class UrlCleanupJob(
    private val urlRepository: UrlRepository,
) {
    @Scheduled(cron = "\${url.cleanup.cron}")
    @SchedulerLock(name = "cleanupExpiredUrls", lockAtMostFor = "5m", lockAtLeastFor = "30s")
    fun cleanupExpiredUrls() {
        val current = Instant.now()
        logger.info("Cleanup started for URLs expired before $current")

        val deleted = urlRepository.deleteByExpiryDateBefore(time = Instant.now())

        logger.info("Deleted $deleted expired URLs at $current")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UrlCleanupJob::class.java)
    }
}
