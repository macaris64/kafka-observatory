package com.kafka.observatory.core.service

import com.kafka.observatory.core.model.ConsumeFrom
import com.kafka.observatory.core.model.ConsumeSession
import com.kafka.observatory.core.model.ConsumeSessionState
import com.kafka.observatory.core.model.ConsumeSessionStatus
import com.kafka.observatory.core.model.ConsumedMessage
import com.kafka.observatory.core.ports.messaging.SessionMessageBroadcaster
import com.kafka.observatory.core.session.ConsumeSessionRegistry
import com.kafka.observatory.ports.kafka.KafkaConsumePort
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Service orchestrating the lifecycle of consume sessions.
 * Pure business logic, depends only on ports and internal registry.
 */
class ConsumeSessionService(
    private val registry: ConsumeSessionRegistry,
    private val kafkaConsumePort: KafkaConsumePort,
    private val broadcaster: SessionMessageBroadcaster,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
        try {
            kafkaConsumePort.startConsumption(session) { message ->
                onMessageConsumed(sessionId, message)
            }
        } catch (e: Exception) {
            handleSessionError(sessionId, e)
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

        // Notify broadcaster to close session
        broadcaster.closeSession(sessionId)

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

    /**
     * Called by the Kafka adapter when a new message is consumed.
     */
    fun onMessageConsumed(
        sessionId: String,
        message: ConsumedMessage,
    ) {
        // 1. Add to buffer
        registry.addMessage(sessionId, message)

        // 2. Broadcast to real-time subscribers
        broadcaster.broadcast(sessionId, message)
    }

    /**
     * Subscribes a listener to a session's message stream.
     */
    fun subscribe(
        sessionId: String,
        onMessage: (ConsumedMessage) -> Unit,
        onClose: () -> Unit = {},
    ): String {
        val session =
            registry.getSession(sessionId)
                ?: throw NoSuchElementException("Session not found: $sessionId")

        if (session.state == ConsumeSessionState.STOPPED || session.state == ConsumeSessionState.ERROR) {
            throw IllegalStateException("Cannot subscribe to an inactive session: $sessionId")
        }

        return broadcaster.subscribe(sessionId, onMessage, onClose)
    }

    /**
     * Unsubscribes from a session's message stream.
     */
    fun unsubscribe(
        sessionId: String,
        subscriptionId: String,
    ) {
        broadcaster.unsubscribe(sessionId, subscriptionId)
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
    ): List<ConsumedMessage> {
        if (registry.getSession(sessionId) == null) {
            throw NoSuchElementException("Session not found: $sessionId")
        }
        return registry.getMessages(sessionId, limit)
    }

    private fun handleSessionError(
        sessionId: String,
        e: Exception,
    ) {
        logger.error("Error in session $sessionId", e)
        registry.updateState(sessionId, ConsumeSessionState.ERROR)
        broadcaster.closeSession(sessionId)
    }
}
