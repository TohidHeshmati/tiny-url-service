package org.tohid.tinyurlservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.tohid.tinyurlservice.controller.dtos.ErrorResponseDTO
import org.tohid.tinyurlservice.controller.dtos.ResolveResponseDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenRequestDTO
import org.tohid.tinyurlservice.controller.dtos.ShortenResponseDTO
import org.tohid.tinyurlservice.domain.toResolveResponseDTO
import org.tohid.tinyurlservice.service.UrlResolverService
import org.tohid.tinyurlservice.service.UrlService
import java.net.URI

@RestController
@RequestMapping("/api/v1/urls")
class UrlApiController(
    private val urlService: UrlService,
    private val urlResolverService: UrlResolverService,
) {
    @PostMapping
    @Operation(summary = "Shorten a URL", description = "Returns a shortened version of the given URL")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Short URL successfully created"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input",
                content = [Content(schema = Schema(implementation = ErrorResponseDTO::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ErrorResponseDTO::class))],
            ),
        ],
    )
    fun shorten(
        @RequestBody @Valid shortenRequestDTO: ShortenRequestDTO,
    ): ResponseEntity<ShortenResponseDTO> {
        val shortenResponse = urlService.shorten(shortenRequestDTO)
        return ResponseEntity.created(URI.create(shortenResponse.shortenedUrl)).body(shortenResponse)
    }

    @GetMapping("/{shortUrl}")
    @Operation(summary = "Resolve a short URL", description = "Returns metadata for a given short URL")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "URL resolved successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Short URL not found or expired",
                content = [Content(schema = Schema(implementation = ErrorResponseDTO::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ErrorResponseDTO::class))],
            ),
        ],
    )
    fun resolve(
        @PathVariable shortUrl: String,
    ): ResponseEntity<ResolveResponseDTO> = ResponseEntity.ok(urlResolverService.resolve(shortUrl).toResolveResponseDTO())
}
