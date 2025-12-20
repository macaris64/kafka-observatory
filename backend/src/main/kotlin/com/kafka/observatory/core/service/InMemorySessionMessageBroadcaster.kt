package com.kafka.observatory.core.service

import com.kafka.observatory.core.model.ConsumedMessage
import com.kafka.observatory.core.ports.messaging.SessionMessageBroadcaster
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory implementation of [SessionMessageBroadcaster].
 * Manages a list of subscribers per session and performs fan-out delivery.
 *
 * Note: Listeners are expected to be non-blocking or handle their own async/backpressure logic
 * to avoid blocking the caller (usually the Kafka poll loop).
 */
class InMemorySessionMessageBroadcaster : SessionMessageBroadcaster {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val subscribers = ConcurrentHashMap<String, CopyOnWriteArrayList<Subscriber>>()

    private data class Subscriber(
        val id: String,
        val onMessage: (ConsumedMessage) -> Unit,
        val onClose: () -> Unit,
    )

    override fun broadcast(
        sessionId: String,
        message: ConsumedMessage,
    ) {
        val sessionSubscribers = subscribers[sessionId] ?: return

        for (subscriber in sessionSubscribers) {
            try {
                subscriber.onMessage(message)
            } catch (e: Exception) {
                logger.error("Error broadcasting message to subscriber ${subscriber.id} in session $sessionId", e)
            }
        }
    }

    override fun subscribe(
        sessionId: String,
        onMessage: (ConsumedMessage) -> Unit,
        onClose: () -> Unit,
    ): String {
        val id = UUID.randomUUID().toString()
        val subscriber = Subscriber(id, onMessage, onClose)
        subscribers.computeIfAbsent(sessionId) { CopyOnWriteArrayList() }.add(subscriber)
        logger.debug("Subscriber $id joined session $sessionId")
        return id
    }

    override fun unsubscribe(
        sessionId: String,
        subscriptionId: String,
    ) {
        subscribers[sessionId]?.removeIf { it.id == subscriptionId }
        logger.debug("Subscriber $subscriptionId left session $sessionId")
    }

    override fun closeSession(sessionId: String) {
        val removed = subscribers.remove(sessionId)
        if (removed != null && removed.isNotEmpty()) {
            logger.info("Closing broadcasting for session $sessionId. Notifying ${removed.size} subscribers.")
            for (subscriber in removed) {
                try {
                    subscriber.onClose()
                } catch (e: Exception) {
                    logger.error("Error notifying subscriber ${subscriber.id} of session $sessionId closure", e)
                }
            }
        }
    }
}
