package org.tohid.tinyurlservice.controller.dtos

import java.io.Serializable
import java.time.Instant
import java.time.Instant.now

data class ShortenResponseDTO(
    val shortenedUrl: String,
    val expiryDate: Instant? = null,
)

data class ResolveResponseDTO(
    val originalUrl: String,
    val expiryDate: Instant? = null,
    val totalClickCount: Long = 0,
) : Serializable

data class ErrorResponseDTO(
    val error: String,
    val time: Instant = now(),
) : Serializable
