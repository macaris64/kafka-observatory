package com.kafka.observatory.adapters.web.rest.exception

import com.kafka.observatory.core.exceptions.ClusterConnectivityException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ClusterConnectivityException::class)
    fun handleClusterConnectivityException(e: ClusterConnectivityException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                error = "Cluster Connectivity Error",
                message = e.message ?: "Failed to connect to Kafka cluster",
            )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                error = "Invalid Argument",
                message = e.message ?: "Invalid request parameters",
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(e: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                error = "Resource Not Found",
                message = e.message ?: "Resource not found",
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                error = "Internal Server Error",
                message = e.message ?: "An unexpected error occurred",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
)
