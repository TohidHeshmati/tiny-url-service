package org.tohid.tinyurlservice.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.tohid.tinyurlservice.repository.UrlRepository
import org.tohid.tinyurlservice.service.UrlLookupCache
import java.time.Instant

@Service
class UrlCleanupJob(
    private val urlRepository: UrlRepository,
    private val urlLookupCache: UrlLookupCache,
) {
    @Scheduled(cron = "\${url.cleanup.cron}")
    @SchedulerLock(name = "cleanupExpiredUrls", lockAtMostFor = "5m", lockAtLeastFor = "30s")
    fun cleanupExpiredUrls() {
        val current = Instant.now()
        logger.info("Cleanup started for URLs expired before $current")

        val deleted = urlRepository.deleteByExpiryDateBefore(time = Instant.now())

        // The bulk delete doesn't know which keys are cached, so clear the URL caches
        // instead of serving deleted links until their cache entries expire.
        if (deleted > 0) {
            urlLookupCache.evictAll()
        }

        logger.info("Deleted $deleted expired URLs at $current")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UrlCleanupJob::class.java)
    }
}
