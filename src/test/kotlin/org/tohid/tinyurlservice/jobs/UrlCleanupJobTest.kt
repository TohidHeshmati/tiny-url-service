package org.tohid.tinyurlservice.jobs

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.tohid.tinyurlservice.repository.UrlRepository
import org.tohid.tinyurlservice.service.UrlLookupCache

class UrlCleanupJobTest {
    private val urlRepository: UrlRepository = mock()
    private val urlLookupCache: UrlLookupCache = mock()
    private val urlCleanupJob = UrlCleanupJob(urlRepository, urlLookupCache)

    @Test
    fun `evicts cached URLs when expired URLs were deleted`() {
        whenever(urlRepository.deleteByExpiryDateBefore(any())).thenReturn(2)

        urlCleanupJob.cleanupExpiredUrls()

        verify(urlLookupCache).evictAll()
    }

    @Test
    fun `leaves the cache alone when nothing was deleted`() {
        whenever(urlRepository.deleteByExpiryDateBefore(any())).thenReturn(0)

        urlCleanupJob.cleanupExpiredUrls()

        verify(urlLookupCache, never()).evictAll()
    }
}
