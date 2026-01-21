package org.tohid.tinyurlservice.controller.dtos

import java.io.Serializable
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit

data class ErrorResponseDTO(
    val error: String,
    val time: Instant = now().truncatedTo(ChronoUnit.SECONDS),
) : Serializable
