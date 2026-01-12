package org.tohid.tinyurlservice.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.tohid.tinyurlservice.BaseIntegrationTest
import org.tohid.tinyurlservice.controller.dtos.DataPoint
import org.tohid.tinyurlservice.controller.dtos.Granularity
import org.tohid.tinyurlservice.controller.dtos.SystemSummaryResponse
import org.tohid.tinyurlservice.controller.dtos.TimeSeriesData
import org.tohid.tinyurlservice.controller.dtos.TopLink
import org.tohid.tinyurlservice.controller.dtos.UrlStatsResponse
import org.tohid.tinyurlservice.service.UrlService
import java.time.Instant
import java.time.temporal.ChronoUnit

@AutoConfigureMockMvc
class UrlStatsControllerIT : BaseIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    override lateinit var urlService: UrlService

    @Test
    fun `gets URL stats successfully`() {
        val shortCode = "abc12"
        val now = Instant.now()
        val statsResponse =
            UrlStatsResponse(
                shortCode = shortCode,
                longUrl = "http://example.com",
                createdAt = now.minus(1, ChronoUnit.DAYS),
                totalClicks = 100,
                timeSeries =
                    TimeSeriesData(
                        granularity = Granularity.DAY,
                        from = now.minus(30, ChronoUnit.DAYS),
                        to = now,
                        dataPoints = listOf(DataPoint(now.truncatedTo(ChronoUnit.DAYS), 100)),
                    ),
            )

        `when`(urlService.getStatsForUrl(shortCode, Granularity.DAY, now.minus(30, ChronoUnit.DAYS), now))
            .thenReturn(statsResponse)

        mockMvc
            .perform(
                get("/api/v1/urls/$shortCode/stats")
                    .param("granularity", "DAY")
                    .param("from", now.minus(30, ChronoUnit.DAYS).toString())
                    .param("to", now.toString()),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.short_code").value(shortCode))
            .andExpect(jsonPath("$.total_clicks").value(100))
    }

    @Test
    fun `gets system summary successfully`() {
        val summaryResponse =
            SystemSummaryResponse(
                totalLinks = 10,
                totalClicksAllTime = 1234,
                topLinks = listOf(TopLink("abc12", 100)),
            )

        `when`(urlService.getSystemSummary()).thenReturn(summaryResponse)

        mockMvc
            .perform(get("/api/v1/stats/summary"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.total_links").value(10))
            .andExpect(jsonPath("$.total_clicks_all_time").value(1234))
    }
}
