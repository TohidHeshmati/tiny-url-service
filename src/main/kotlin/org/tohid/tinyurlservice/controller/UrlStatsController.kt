package org.tohid.tinyurlservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.tohid.tinyurlservice.controller.dtos.Granularity
import org.tohid.tinyurlservice.controller.dtos.UrlStatsResponseDTO
import org.tohid.tinyurlservice.service.UrlService
import java.time.Instant
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/v1")
class UrlStatsController(
    private val urlService: UrlService,
) {
    @GetMapping("/urls/{shortCode}/stats")
    @Operation(summary = "Get statistics for a short URL")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Short URL not found"),
        ],
    )
    fun getUrlStats(
        @PathVariable shortCode: String,
        @RequestParam(required = false, defaultValue = "DAY") granularity: Granularity,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
    ): ResponseEntity<UrlStatsResponseDTO> {
        val fromDate = from ?: Instant.now().minus(30, ChronoUnit.DAYS)
        val toDate = to ?: Instant.now()
        val stats = urlService.getStatsForUrl(shortCode, granularity, fromDate, toDate)
        return ResponseEntity.ok(stats)
    }
}
