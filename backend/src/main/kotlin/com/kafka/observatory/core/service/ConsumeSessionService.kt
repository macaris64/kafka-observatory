package com.kafka.observatory.core.service

import com.kafka.observatory.core.model.ConsumeFrom
import com.kafka.observatory.core.model.ConsumeSession
import com.kafka.observatory.core.model.ConsumeSessionState
import com.kafka.observatory.core.model.ConsumeSessionStatus
import com.kafka.observatory.core.session.ConsumeSessionRegistry
import com.kafka.observatory.ports.kafka.KafkaConsumePort
import java.util.UUID

/**
 * Service orchestrating the lifecycle of consume sessions.
 * Pure business logic, depends only on ports and internal registry.
 */
class ConsumeSessionService(
    private val registry: ConsumeSessionRegistry,
    private val kafkaConsumePort: KafkaConsumePort,
) {
    fun startSession(
        topic: String,
        groupId: String?,
        from: ConsumeFrom,
        maxBufferSize: Int,
    ): ConsumeSession {
        // Generate ID
        val sessionId = "sess_${UUID.randomUUID()}"
        val finalGroupId = groupId ?: "kafka-observatory-$sessionId"

        val session =
            ConsumeSession(
                id = sessionId,
                topic = topic,
                groupId = finalGroupId,
                from = from,
                maxBufferSize = maxBufferSize,
                state = ConsumeSessionState.RUNNING,
            )

        registry.register(session)

        // Start consumption via port
        // Note: If port fails immediately, we might want to catch and handle,
        // but for now we assume async start or exception propagation.
        try {
            kafkaConsumePort.startConsumption(session)
        } catch (e: Exception) {
            registry.updateState(sessionId, ConsumeSessionState.ERROR)
            throw e
        }

        return session
    }

    fun stopSession(sessionId: String): ConsumeSession {
        val session =
            registry.getSession(sessionId)
                ?: throw NoSuchElementException("Session not found: $sessionId")

        kafkaConsumePort.stopConsumption(sessionId)
        registry.updateState(sessionId, ConsumeSessionState.STOPPED)

        return registry.getSession(sessionId)!!
    }

    fun getSession(sessionId: String): ConsumeSession? {
        return registry.getSession(sessionId)
    }

    fun pauseSession(sessionId: String): ConsumeSession {
        val session =
            registry.getSession(sessionId)
                ?: throw NoSuchElementException("Session not found: $sessionId")

        if (session.state == ConsumeSessionState.STOPPED) {
            throw IllegalStateException("Cannot pause a stopped session")
        }

        kafkaConsumePort.pauseConsumption(sessionId)
        registry.updateState(sessionId, ConsumeSessionState.PAUSED)

        return registry.getSession(sessionId)!!
    }

    fun resumeSession(sessionId: String): ConsumeSession {
        val session =
            registry.getSession(sessionId)
                ?: throw NoSuchElementException("Session not found: $sessionId")

        if (session.state == ConsumeSessionState.STOPPED) {
            throw IllegalStateException("Cannot resume a stopped session: $sessionId")
        }

        kafkaConsumePort.resumeConsumption(sessionId)
        registry.updateState(sessionId, ConsumeSessionState.RUNNING)

        return registry.getSession(sessionId)!!
    }

    fun getStatus(sessionId: String): ConsumeSessionStatus {
        val session =
            registry.getSession(sessionId)
                ?: throw NoSuchElementException("Session not found: $sessionId")

        return ConsumeSessionStatus(
            sessionId = session.id,
            topic = session.topic,
            state = session.state,
            lastConsumedAt = session.lastConsumedAt,
            bufferSize = registry.getBufferSize(sessionId),
        )
    }

    fun checkIdleSessions(maxIdleDuration: java.time.Duration) {
        val now = java.time.Instant.now()
        registry.getAllSessions().forEach { session ->
            if (session.state == ConsumeSessionState.RUNNING || session.state == ConsumeSessionState.PAUSED) {
                val lastActivity = session.lastConsumedAt ?: session.createdAt
                if (java.time.Duration.between(lastActivity, now) > maxIdleDuration) {
                    stopSession(session.id)
                }
            }
        }
    }

    fun getMessages(
        sessionId: String,
        limit: Int,
    ): List<com.kafka.observatory.core.model.ConsumedMessage> {
        if (registry.getSession(sessionId) == null) {
            throw NoSuchElementException("Session not found: $sessionId")
        }
        return registry.getMessages(sessionId, limit)
    }
}
