package org.tohid.tinyurlservice.handler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.tohid.tinyurlservice.controller.dtos.ErrorResponseDTO
import org.tohid.tinyurlservice.exception.NotFoundException

@ControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun handle404(ex: NotFoundException): ResponseEntity<ErrorResponseDTO> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDTO(ex.message ?: "Not found"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDTO> {
        val message =
            ex.bindingResult
                .fieldErrors
                .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDTO(message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidJson(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponseDTO> {
        val cause = ex.cause
        val message =
            if (cause is InvalidFormatException) {
                "${cause.path.joinToString(".") { it.fieldName }}: ${cause.originalMessage}"
            } else {
                "Malformed request body"
            }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDTO(message))
    }
}
