package com.kafka.observatory.ports.kafka

import com.kafka.observatory.core.model.ConsumeSession

/**
 * Port interface for managing the low-level Kafka consumption process.
 * Acts as the Secondary Port in the architecture, implemented by the Kafka Adapter.
 */
interface KafkaConsumePort {
    /**
     * Starts a background consumption process for the given session.
     */
    fun startConsumption(
        session: ConsumeSession,
        onMessage: (com.kafka.observatory.core.model.ConsumedMessage) -> Unit,
    )

    /**
     * Stops the background consumption process for the given session ID.
     */
    fun stopConsumption(sessionId: String)

    /**
     * Pauses the background consumption process for the given session ID.
     */
    fun pauseConsumption(sessionId: String)

    /**
     * Resumes the background consumption process for the given session ID.
     */
    fun resumeConsumption(sessionId: String)
}
