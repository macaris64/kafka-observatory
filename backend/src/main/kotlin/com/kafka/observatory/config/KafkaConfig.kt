package com.kafka.observatory.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(
    @Value("\${spring.profiles.active:default}") private val activeProfile: String,
) {
    @PostConstruct
    fun logConfig() {
        println("Kafka Observatory started with profile: \$activeProfile")
    }

    // TODO: Implement logic to read Kafka configuration from environment variables
    // TODO: Build common Kafka properties
}
