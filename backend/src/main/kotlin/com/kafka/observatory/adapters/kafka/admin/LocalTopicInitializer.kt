package com.kafka.observatory.adapters.kafka.admin

import com.kafka.observatory.adapters.kafka.KafkaConnectionManager
import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local")
@EnableConfigurationProperties(LocalTopicProperties::class)
class LocalTopicInitializer(
    private val kafkaConnectionManager: KafkaConnectionManager,
    private val properties: LocalTopicProperties,
) {
    private val logger = LoggerFactory.getLogger(LocalTopicInitializer::class.java)

    @PostConstruct
    fun initializeTopics() {
        if (properties.initialTopics.isEmpty()) {
            logger.info("No initial topics configured for local environment.")
            return
        }

        try {
            val adminClient = kafkaConnectionManager.getAdminClient()
            val existingTopics = adminClient.listTopics().names().get()

            val newTopics =
                properties.initialTopics
                    .filter { it.name !in existingTopics }
                    .map {
                        NewTopic(it.name, it.partitions, it.replicationFactor.toShort())
                    }

            if (newTopics.isNotEmpty()) {
                logger.info("Creating ${newTopics.size} initial topics for local environment: $newTopics")
                adminClient.createTopics(newTopics).all().get()
                logger.info("Successfully created initial topics.")
            } else {
                logger.info("All initial topics already exist.")
            }
        } catch (e: Exception) {
            logger.warn("Failed to initialize local topics: ${e.message}", e)
        }
    }
}

@ConfigurationProperties(prefix = "kafka.observatory")
data class LocalTopicProperties(
    val initialTopics: List<TopicConfig> = emptyList(),
)

data class TopicConfig(
    val name: String,
    val partitions: Int,
    val replicationFactor: Int,
)
