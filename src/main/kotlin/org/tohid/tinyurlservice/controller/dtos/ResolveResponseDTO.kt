package org.tohid.tinyurlservice.controller.dtos

import java.io.Serializable
import java.time.Instant

data class ResolveResponseDTO(
    val originalUrl: String,
    val expiryDate: Instant? = null,
    val totalClickCount: Long = 0,
) : Serializable
