package com.kafka.observatory.integration

import com.kafka.observatory.adapters.kafka.KafkaConnectionManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
@ActiveProfiles("local")
class LocalTopicInitializerTest {
    @Autowired
    private lateinit var kafkaConnectionManager: KafkaConnectionManager

    companion object {
        @Container
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

        @JvmStatic
        @DynamicPropertySource
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("KAFKA_BROKERS") { kafka.bootstrapServers }
        }
    }

    @Test
    fun `should create initial topics on startup in local profile`() {
        // Validation: Check if topics defined in application-local.yml exist
        val admin = kafkaConnectionManager.getAdminClient()
        val existingTopics = admin.listTopics().names().get()

        val expectedTopics = listOf("orders", "payments", "customers", "inventory", "notifications")

        expectedTopics.forEach { topic ->
            assertTrue(existingTopics.contains(topic), "Topic $topic should have been created automatically")
        }
    }
}
