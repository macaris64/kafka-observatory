package com.kafka.observatory.adapters.web.rest

import com.kafka.observatory.core.model.ConsumeFrom
import com.kafka.observatory.core.service.ConsumeSessionService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class CreateSessionRequest(
    val topic: String,
    val groupId: String? = null,
    val from: String? = null, // "EARLIEST" or "LATEST"
    val maxBufferSize: Int? = null,
)

@RestController
@RequestMapping("/api/consume-sessions")
class ConsumeSessionController(
    private val service: ConsumeSessionService,
) {
    @Operation(summary = "Start a new consume session")
    @PostMapping
    fun startSession(
        @RequestBody request: CreateSessionRequest,
    ): ResponseEntity<Map<String, Any>> {
        val from =
            if (request.from != null) {
                try {
                    ConsumeFrom.valueOf(request.from.uppercase())
                } catch (e: IllegalArgumentException) {
                    ConsumeFrom.LATEST // Default or throw bad request?
                    // Let's default to LATEST if invalid or null, or throw?
                    // Prompt says "from supports: EARLIEST or LATEST".
                    // Better to throw 400 if invalid enum.
                    throw IllegalArgumentException("Invalid 'from' value. Must be EARLIEST or LATEST")
                }
            } else {
                ConsumeFrom.LATEST
            }

        val session =
            service.startSession(
                topic = request.topic,
                groupId = request.groupId,
                from = from,
                maxBufferSize = request.maxBufferSize ?: 500,
            )

        return ResponseEntity.ok(mapOf("data" to session))
    }

    @Operation(summary = "Get buffered messages for a session")
    @GetMapping("/{sessionId}/messages")
    fun getMessages(
        @PathVariable sessionId: String,
        @RequestParam(defaultValue = "100") limit: Int,
    ): ResponseEntity<Map<String, Any>> {
        val messages = service.getMessages(sessionId, limit)
        return ResponseEntity.ok(mapOf("data" to messages))
    }

    @Operation(summary = "Stop and remove a consume session")
    @DeleteMapping("/{sessionId}")
    fun stopSession(
        @PathVariable sessionId: String,
    ): ResponseEntity<Map<String, Any>> {
        val session = service.stopSession(sessionId)
        return ResponseEntity.ok(mapOf("data" to session))
    }

    @Operation(summary = "Pause a consume session")
    @PostMapping("/{sessionId}/pause")
    fun pauseSession(
        @PathVariable sessionId: String,
    ): ResponseEntity<Map<String, Any>> {
        val session = service.pauseSession(sessionId)
        return ResponseEntity.ok(mapOf("data" to session))
    }

    @Operation(summary = "Resume a paused consume session")
    @PostMapping("/{sessionId}/resume")
    fun resumeSession(
        @PathVariable sessionId: String,
    ): ResponseEntity<Map<String, Any>> {
        val session = service.resumeSession(sessionId)
        return ResponseEntity.ok(mapOf("data" to session))
    }

    @Operation(summary = "Get consume session status")
    @GetMapping("/{sessionId}")
    fun getSessionStatus(
        @PathVariable sessionId: String,
    ): ResponseEntity<Map<String, Any>> {
        val status = service.getStatus(sessionId)
        return ResponseEntity.ok(mapOf("data" to status))
    }
}
