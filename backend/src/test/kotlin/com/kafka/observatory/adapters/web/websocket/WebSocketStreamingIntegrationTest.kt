package com.kafka.observatory.adapters.web.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.kafka.observatory.core.model.ConsumedMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class WebSocketStreamingIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

        @JvmStatic
        @DynamicPropertySource
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("KAFKA_BROKERS") { kafka.bootstrapServers }
        }
    }

    @Test
    fun `should stream messages via WebSocket`() {
        val topic = "streaming-test-topic"

        // 1. Start a consume session via REST
        val startRequest =
            mapOf(
                "topic" to topic,
                "from" to "EARLIEST",
                "maxBufferSize" to 100,
            )
        val sessionResponse = restTemplate.postForEntity("/api/consume-sessions", startRequest, Map::class.java)
        assertEquals(200, sessionResponse.statusCode.value()) { "Failed to start session: ${sessionResponse.body}" }

        val responseBody = sessionResponse.body
        assertNotNull(responseBody, "Session response body is null")

        @Suppress("UNCHECKED_CAST")
        val data = responseBody!!["data"] as Map<String, Any>
        val sessionId = data["id"] as String
        assertNotNull(sessionId)

        // 2. Connect via WebSocket
        val client = StandardWebSocketClient()
        val messagesQueue = LinkedBlockingQueue<ConsumedMessage>()

        val handler =
            object : TextWebSocketHandler() {
                override fun handleTextMessage(
                    session: WebSocketSession,
                    message: TextMessage,
                ) {
                    val consumed = objectMapper.readValue(message.payload, ConsumedMessage::class.java)
                    messagesQueue.offer(consumed)
                }
            }

        val url = "ws://localhost:$port/ws/consume-sessions/$sessionId"
        val wsSession = client.execute(handler, url).get(10, TimeUnit.SECONDS)
        assertNotNull(wsSession)

        // 3. Produce messages to Kafka
        val producerProps =
            Properties().apply {
                put("bootstrap.servers", kafka.bootstrapServers)
                put("key.serializer", StringSerializer::class.java.name)
                put("value.serializer", StringSerializer::class.java.name)
            }
        KafkaProducer<String, String>(producerProps).use { producer ->
            producer.send(ProducerRecord(topic, "key-1", "value-1")).get()
            producer.send(ProducerRecord(topic, "key-2", "value-2")).get()
        }

        // 4. Verify messages are received via WebSocket
        // Giving some extra time for Kafka poll cycle and WebSocket transport
        val m1 = messagesQueue.poll(20, TimeUnit.SECONDS)
        assertNotNull(m1, "Message 1 not received via WebSocket")
        assertEquals("value-1", m1?.value)

        val m2 = messagesQueue.poll(10, TimeUnit.SECONDS)
        assertNotNull(m2, "Message 2 not received via WebSocket")
        assertEquals("value-2", m2?.value)

        // 5. Cleanup
        wsSession.close()
        restTemplate.delete("/api/consume-sessions/$sessionId")
    }
}
