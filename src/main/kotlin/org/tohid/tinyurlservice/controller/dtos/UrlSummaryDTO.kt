package org.tohid.tinyurlservice.controller.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class UrlSummaryDTO(
    @JsonProperty("short_code")
    val shortCode: String,
    @JsonProperty("original_url")
    val originalUrl: String,
    @JsonProperty("total_clicks")
    val totalClicks: Long,
)
