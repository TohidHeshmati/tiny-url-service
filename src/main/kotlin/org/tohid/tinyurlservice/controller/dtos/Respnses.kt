package org.tohid.tinyurlservice.controller.dtos

import java.io.Serializable
import java.time.Instant
import java.time.Instant.now

data class ResolveResponseDTO(
    val originalUrl: String,
    val expiryDate: Instant? = null,
) : Serializable

data class ErrorResponseDTO(
    val error: String,
    val time: Instant = now(),
) : Serializable
