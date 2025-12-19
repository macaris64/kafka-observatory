package com.kafka.observatory.adapters.kafka

import com.kafka.observatory.config.KafkaConfig
import org.apache.kafka.clients.admin.AdminClient
import org.springframework.stereotype.Component

@Component
class KafkaConnectionManager(
    private val kafkaConfig: KafkaConfig,
) {
    private val internalAdminClient: AdminClient by lazy {
        try {
            AdminClient.create(kafkaConfig.buildCommonProperties())
        } catch (e: Exception) {
            // Wrapping in a generic runtime exception as per prompt instructions to not crash immediately until used?
            // "The application must be able to start ... without crashing."
            // Lazy loading ensures it doesn't crash on startup unless touched.
            // If creation fails inside lazy, it will throw when accessed.
            throw RuntimeException("Failed to create Kafka AdminClient", e)
        }
    }

    @Synchronized
    fun getAdminClient(): AdminClient {
        return internalAdminClient
    }
}
