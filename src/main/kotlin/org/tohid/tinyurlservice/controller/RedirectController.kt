package org.tohid.tinyurlservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.tohid.tinyurlservice.controller.dtos.ErrorResponseDTO
import org.tohid.tinyurlservice.service.UrlService

@RestController
@RequestMapping("/")
class RedirectController(
    private val urlService: UrlService,
) {
    @GetMapping("/{shortUrl}")
    @Operation(summary = "Redirect", description = "Redirects to the original URL (301 for permanent, 302 for temporary)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "301", description = "Permanent redirect"),
            ApiResponse(responseCode = "302", description = "Temporary redirect"),
            ApiResponse(
                responseCode = "404",
                description = "Not found",
                content = [Content(schema = Schema(implementation = ErrorResponseDTO::class))],
            ),
        ],
    )
    fun redirect(
        @PathVariable shortUrl: String,
    ): ResponseEntity<Void> = urlService.redirectsByShortUrl(shortUrl)
}
