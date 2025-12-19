package com.kafka.observatory.integration

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.Properties

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class TopicIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

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
    fun `should list topics from kafka`() {
        // Setup: Create a topic strictly for this test
        val topicName = "integration-test-topic"
        createTopic(topicName, 2, 1)

        mockMvc.perform(
            get("/api/topics")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[?(@.name == '$topicName')]").exists())
            .andExpect(jsonPath("$.data[?(@.name == '$topicName')].partitionCount").value(2))
    }

    private fun createTopic(
        name: String,
        partitions: Int,
        replicationFactor: Short,
    ) {
        val props = Properties()
        props["bootstrap.servers"] = kafka.bootstrapServers
        val admin = AdminClient.create(props)
        admin.use {
            it.createTopics(listOf(NewTopic(name, partitions, replicationFactor))).all().get()
        }
    }
}
