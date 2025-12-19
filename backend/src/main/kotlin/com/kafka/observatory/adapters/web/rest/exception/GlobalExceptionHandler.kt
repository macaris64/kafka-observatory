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
