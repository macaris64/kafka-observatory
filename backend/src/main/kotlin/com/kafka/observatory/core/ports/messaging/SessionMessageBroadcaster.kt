package com.kafka.observatory.core.ports.messaging

import com.kafka.observatory.core.model.ConsumedMessage

/**
 * Port for broadcasting messages from a consume session to subscribers.
 */
interface SessionMessageBroadcaster {
    /**
     * Broadcasts a message to all subscribers of the given session.
     */
    fun broadcast(
        sessionId: String,
        message: ConsumedMessage,
    )

    /**
     * Subscribes a listener to messages from the given session.
     * The [onMessage] listener will be called whenever a new message is broadcasted.
     * The [onClose] listener will be called when the session is closed via [closeSession].
     * Returns a subscription ID that can be used to unsubscribe.
     */
    fun subscribe(
        sessionId: String,
        onMessage: (ConsumedMessage) -> Unit,
        onClose: () -> Unit = {},
    ): String

    /**
     * Unsubscribes a listener from the given session.
     */
    fun unsubscribe(
        sessionId: String,
        subscriptionId: String,
    )

    /**
     * Closes all subscriptions for the given session.
     */
    fun closeSession(sessionId: String)
}
