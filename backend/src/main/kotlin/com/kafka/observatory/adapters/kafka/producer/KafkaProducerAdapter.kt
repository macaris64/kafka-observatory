package com.kafka.observatory.adapters.kafka.producer

import com.kafka.observatory.config.KafkaConfig
import com.kafka.observatory.core.model.ProduceRequest
import com.kafka.observatory.core.model.ProduceResponse
import com.kafka.observatory.ports.kafka.KafkaProducerPort
import jakarta.annotation.PreDestroy
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KafkaProducerAdapter(
    private val kafkaConfig: KafkaConfig,
) : KafkaProducerPort {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val producer: KafkaProducer<String, String> by lazy {
        val props = kafkaConfig.buildCommonProperties()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        // Ensure some reliability
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.RETRIES_CONFIG] = 3

        logger.info("Initializing Kafka Producer")
        KafkaProducer<String, String>(props)
    }

    override fun produce(request: ProduceRequest): ProduceResponse {
        val record =
            ProducerRecord(
                request.topic,
                request.partition,
                request.key,
                request.value,
            )

        request.headers?.forEach { (k, v) ->
            record.headers().add(RecordHeader(k, v.toByteArray()))
        }

        return try {
            val metadata = producer.send(record).get()
            ProduceResponse(
                topic = metadata.topic(),
                partition = metadata.partition(),
                offset = metadata.offset(),
                timestamp = metadata.timestamp(),
            )
        } catch (e: Exception) {
            logger.error("Failed to produce message to topic ${request.topic}", e)
            throw e
        }
    }

    @PreDestroy
    fun close() {
        logger.info("Closing Kafka Producer")
        try {
            producer.close()
        } catch (e: Exception) {
            logger.error("Error closing Kafka Producer", e)
        }
    }
}
