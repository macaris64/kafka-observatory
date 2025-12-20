package com.kafka.observatory.adapters.web.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.kafka.observatory.core.model.ConsumedMessage
import com.kafka.observatory.core.service.ConsumeSessionService
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * WebSocket handler that streams messages from a specific consume session to connected clients.
 * Path format: /ws/consume-sessions/{sessionId}
 */
class SessionWebSocketHandler(
    private val sessionService: ConsumeSessionService,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Map of WebSocket session ID to the subscription ID in the core service
    private val subscriptions = ConcurrentHashMap<String, String>()

    // Locks to prevent concurrent sends to the same WebSocket session and handle backpressure
    private val sessionLocks = ConcurrentHashMap<String, ReentrantLock>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = extractSessionId(session)
        if (sessionId == null) {
            logger.warn("WebSocket connection attempt without sessionId in path")
            session.close(CloseStatus.BAD_DATA.withReason("Session ID missing in path"))
            return
        }

        try {
            // Subscribe to the session message stream
            val subscriptionId =
                sessionService.subscribe(
                    sessionId,
                    onMessage = { message -> sendMessageToClient(session, message) },
                    onClose = {
                        logger.info("Session $sessionId closed. Closing WebSocket ${session.id}")
                        try {
                            session.close(CloseStatus.NORMAL.withReason("Consume session stopped"))
                        } catch (e: Exception) {
                            logger.warn("Error closing WebSocket ${session.id}: ${e.message}")
                        }
                    },
                )

            subscriptions[session.id] = subscriptionId
            sessionLocks[session.id] = ReentrantLock()

            logger.info("WebSocket client ${session.id} subscribed to consume session $sessionId")
        } catch (e: NoSuchElementException) {
            logger.warn("Rejecting WebSocket: session $sessionId not found")
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Session $sessionId not found"))
        } catch (e: IllegalStateException) {
            logger.warn("Rejecting WebSocket: session $sessionId is ${e.message}")
            session.close(CloseStatus.POLICY_VIOLATION.withReason(e.message ?: "Illegal session state"))
        } catch (e: Exception) {
            logger.error("Error establishing WebSocket subscription for session $sessionId", e)
            session.close(CloseStatus.SERVER_ERROR)
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        val sessionId = extractSessionId(session)
        val subscriptionId = subscriptions.remove(session.id)
        sessionLocks.remove(session.id)

        if (sessionId != null && subscriptionId != null) {
            sessionService.unsubscribe(sessionId, subscriptionId)
            logger.info("WebSocket client ${session.id} disconnected from session $sessionId")
        }
    }

    private fun sendMessageToClient(
        session: WebSocketSession,
        message: ConsumedMessage,
    ) {
        if (!session.isOpen) return

        val lock = sessionLocks[session.id] ?: return

        // MVP Backpressure: If the client is slow (send in progress), drop the message
        // to avoid blocking the Kafka poll loop.
        if (lock.tryLock()) {
            try {
                val payload = objectMapper.writeValueAsString(message)
                session.sendMessage(TextMessage(payload))
            } catch (e: IOException) {
                logger.warn("Failed to send message to WS client ${session.id}: ${e.message}")
                // Disconnect likely broken connection
                try {
                    session.close()
                } catch (ex: Exception) {
                }
            } catch (e: Exception) {
                logger.error("Unexpected error sending message to WS client ${session.id}", e)
            } finally {
                lock.unlock()
            }
        } else {
            // Client is slow, dropping message as per requirements
            if (logger.isTraceEnabled) {
                logger.trace("Dropping message for slow WebSocket client ${session.id}")
            }
        }
    }

    private fun extractSessionId(session: WebSocketSession): String? {
        val path = session.uri?.path ?: return null
        // Format: /ws/consume-sessions/{sessionId}
        return path.substringAfterLast("/")
    }
}
