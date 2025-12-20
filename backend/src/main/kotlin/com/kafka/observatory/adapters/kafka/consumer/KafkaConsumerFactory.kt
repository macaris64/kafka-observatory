package com.kafka.observatory.adapters.kafka.consumer

import com.kafka.observatory.config.KafkaConfig
import com.kafka.observatory.core.model.ConsumeFrom
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Component

@Component
class KafkaConsumerFactory(
    private val kafkaConfig: KafkaConfig,
) {
    fun createConsumer(
        groupId: String,
        from: ConsumeFrom,
    ): KafkaConsumer<String, String> {
        val props = kafkaConfig.buildCommonProperties()

        // Consumer specific overrides
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name

        // Auto offset reset
        // If the group exists, it resumes. If new random group, it respects this.
        val reset = if (from == ConsumeFrom.EARLIEST) "earliest" else "latest"
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = reset

        // Disable auto commit if we want purely ephemeral view?
        // Prompt says "Controlled, session-based lifecycle".
        // Usually observability tools don't want to mess up offsets for other consumer groups,
        // but here we generate random groups or use specific ones.
        // Let's enable auto-commit for now as is standard, but maybe strictly we shouldn't commit if we are just "observing" with a random group.
        // For a specific group provided by user, they might expect commit.
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"

        return KafkaConsumer(props)
    }
}
