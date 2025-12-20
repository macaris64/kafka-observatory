package com.kafka.observatory.config

import com.kafka.observatory.core.ports.messaging.SessionMessageBroadcaster
import com.kafka.observatory.core.service.ConsumeSessionService
import com.kafka.observatory.core.service.InMemorySessionMessageBroadcaster
import com.kafka.observatory.core.session.ConsumeSessionRegistry
import com.kafka.observatory.ports.kafka.KafkaConsumePort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun consumeSessionRegistry(): ConsumeSessionRegistry {
        return ConsumeSessionRegistry()
    }

    @Bean
    fun sessionMessageBroadcaster(): SessionMessageBroadcaster {
        return InMemorySessionMessageBroadcaster()
    }

    @Bean
    fun consumeSessionService(
        registry: ConsumeSessionRegistry,
        kafkaConsumePort: KafkaConsumePort,
        broadcaster: SessionMessageBroadcaster,
    ): ConsumeSessionService {
        return ConsumeSessionService(registry, kafkaConsumePort, broadcaster)
    }
}
