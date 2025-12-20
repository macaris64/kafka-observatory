package com.kafka.observatory.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.kafka.observatory.core.model.ConsumedMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
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
class KafkaObservatoryE2ETest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))

        @JvmStatic
        @DynamicPropertySource
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("KAFKA_BROKERS") { kafka.bootstrapServers }
        }
    }

    @Test
    fun `should complete the full client journey successfully`() {
        val topic = "e2e-test-topic"

        // 1. Verify /health returns UP
        val healthResponse = restTemplate.getForEntity("/api/health", Map::class.java)
        assertEquals(200, healthResponse.statusCode.value())
        assertEquals("UP", healthResponse.body?.get("status"))

        // 2. Call /cluster and verify Kafka connectivity
        val clusterResponse = restTemplate.getForEntity("/api/cluster", Map::class.java)
        assertEquals(200, clusterResponse.statusCode.value())
        val clusterData = clusterResponse.body?.get("data") as Map<*, *>
        assertNotNull(clusterData["clusterId"])

        // 3. Call /topics and verify test topic is present (or at least no error)
        // We create a topic first to be sure
        produceMessages(topic, 1)
        val topicsResponse = restTemplate.getForEntity("/api/topics", Map::class.java)
        assertEquals(200, topicsResponse.statusCode.value())
        val topics = topicsResponse.body?.get("data") as List<*>
        assertTrue(topics.any { (it as Map<*, *>)["name"] == topic })

        // 4. Create a consume session via /consume-sessions
        val startRequest =
            mapOf(
                "topic" to topic,
                "from" to "EARLIEST",
                "maxBufferSize" to 100,
            )
        val sessionResponse = restTemplate.postForEntity("/api/consume-sessions", startRequest, Map::class.java)
        assertEquals(200, sessionResponse.statusCode.value())
        val sessionData = sessionResponse.body?.get("data") as Map<*, *>
        val sessionId = sessionData["id"] as String
        assertNotNull(sessionId)

        // 5. Open a WebSocket connection
        val wsClient = StandardWebSocketClient()
        val wsMessages = LinkedBlockingQueue<ConsumedMessage>()
        val wsHandler =
            object : TextWebSocketHandler() {
                override fun handleTextMessage(
                    session: WebSocketSession,
                    message: TextMessage,
                ) {
                    wsMessages.offer(objectMapper.readValue(message.payload, ConsumedMessage::class.java))
                }
            }
        val wsUrl = "ws://localhost:$port/ws/consume-sessions/$sessionId"
        val wsSession = wsClient.execute(wsHandler, wsUrl).get(10, TimeUnit.SECONDS)
        assertTrue(wsSession.isOpen)

        // 6. Produce Kafka messages
        produceMessages(topic, 3, "msg-")

        // 7. Verify messages are received via WebSocket
        val m1 = wsMessages.poll(15, TimeUnit.SECONDS)
        assertNotNull(m1, "Message 1 should be received via WebSocket")

        // 8. Pause the consume session
        val pauseResponse = restTemplate.postForEntity("/api/consume-sessions/$sessionId/pause", null, Map::class.java)
        assertEquals(200, pauseResponse.statusCode.value())
        val pausedData = pauseResponse.body?.get("data") as Map<*, *>
        assertEquals("PAUSED", pausedData["state"])

        // Wait for pause to propagate in adapter
        Thread.sleep(2000)
        wsMessages.clear()

        // Produce messages while paused
        produceMessages(topic, 2, "paused-")

        // Verify no new WebSocket messages arrive
        val pausedMsg = wsMessages.poll(3, TimeUnit.SECONDS)
        assertTrue(pausedMsg == null, "No messages should arrive while paused")

        // 9. Resume the session
        val resumeResponse = restTemplate.postForEntity("/api/consume-sessions/$sessionId/resume", null, Map::class.java)
        assertEquals(200, resumeResponse.statusCode.value())
        val resumedData = resumeResponse.body?.get("data") as Map<*, *>
        assertEquals("RUNNING", resumedData["state"])

        // 10. Verify streaming resumes
        val resumedMsg = wsMessages.poll(10, TimeUnit.SECONDS)
        assertNotNull(resumedMsg, "Streaming should resume and deliver messages")

        // 11. Fetch messages via REST snapshot endpoint
        val restMessagesResponse = restTemplate.getForEntity("/api/consume-sessions/$sessionId/messages", Map::class.java)
        assertEquals(200, restMessagesResponse.statusCode.value())
        val restMessages = restMessagesResponse.body?.get("data") as List<*>
        assertTrue(restMessages.isNotEmpty(), "REST snapshot should not be empty")

        // 12. Stop the consume session
        val deleteResponse = restTemplate.exchange("/api/consume-sessions/$sessionId", HttpMethod.DELETE, HttpEntity.EMPTY, Map::class.java)
        assertEquals(200, deleteResponse.statusCode.value())
        val stoppedData = deleteResponse.body?.get("data") as Map<*, *>
        assertEquals("STOPPED", stoppedData["state"])

        // 13. Verify WebSocket connection closes
        // We give it a little time to process the stop event
        var closed = false
        for (i in 1..20) {
            if (!wsSession.isOpen) {
                closed = true
                break
            }
            Thread.sleep(500)
        }
        assertTrue(closed, "WebSocket session should be closed after consume session stops")
    }

    private fun produceMessages(
        topic: String,
        count: Int,
        prefix: String = "test-",
    ) {
        val props = Properties()
        props["bootstrap.servers"] = kafka.bootstrapServers
        props["key.serializer"] = StringSerializer::class.java.name
        props["value.serializer"] = StringSerializer::class.java.name

        KafkaProducer<String, String>(props).use { producer ->
            for (i in 1..count) {
                producer.send(ProducerRecord(topic, "key-$i", "$prefix$i")).get()
            }
        }
    }
}
