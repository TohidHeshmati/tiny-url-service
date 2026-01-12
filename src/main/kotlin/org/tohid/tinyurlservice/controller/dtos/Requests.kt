package org.tohid.tinyurlservice.controller.dtos

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import org.tohid.tinyurlservice.utils.SafeInstantDeserializer
import org.tohid.tinyurlservice.validators.FutureInstant
import java.time.Instant

data class ShortenResponseDTO(
    val shortenedUrl: String,
    val expiryDate: Instant? = null,
    val totalClickCount: Long = 0,
)

data class UrlResponse(
    val shortCode: String,
    val originalUrl: String,
    val createdAt: Instant,
    val totalClickCount: Long,
)

data class ShortenRequestDTO(
    @field:NotBlank(message = "URL must not be blank")
    @field:URL(message = "Must be a valid URL")
    @field:Size(max = 512, message = "URL must not exceed 512 characters")
    val originalUrl: String,
    @field:FutureInstant
    @field:JsonDeserialize(using = SafeInstantDeserializer::class)
    val expiryDate: Instant? = null,
)
