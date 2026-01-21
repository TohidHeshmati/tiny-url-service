package org.tohid.tinyurlservice.controller.dtos

import java.time.Instant

data class ShortenResponseDTO(
    val shortenedUrl: String,
    val expiryDate: Instant? = null,
)
