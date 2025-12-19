package com.kafka.observatory.adapters.kafka

import com.kafka.observatory.config.KafkaConfig
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class KafkaConnectionManagerTest {
    @Test
    fun `getAdminClient should create client lazily`() {
        // Mocking KafkaConfig manually without Mockk to keep dependencies simple as requester might not have mockk
        // Using manual object creation and ReflectionTestUtils
        val config = KafkaConfig("test")
        ReflectionTestUtils.setField(config, "brokers", "localhost:9092")
        // We set values to avoid NPEs if logic accesses them
        ReflectionTestUtils.setField(config, "tlsEnabled", false)
        ReflectionTestUtils.setField(config, "saslEnabled", false)

        val manager = KafkaConnectionManager(config)

        // It should not throw on creation
        assertNotNull(manager)

        // When accessing getAdminClient, it will try to connect.
        // Since localhost:9092 might not be running, AdminClient.create() usually doesn't connect immediately but returns the client.
        // It connects in background. So this should pass without exception unless config is invalid.
        val client = manager.getAdminClient()
        assertNotNull(client)
    }

    @Test
    fun `getAdminClient should fail if config is invalid`() {
        val config = KafkaConfig("test")
        // No brokers set -> buildCommonProperties throws Exception
        ReflectionTestUtils.setField(config, "brokers", "")

        val manager = KafkaConnectionManager(config)

        // Should throw RuntimeException wrapping the IllegalStateException from config
        val exception =
            assertThrows(RuntimeException::class.java) {
                manager.getAdminClient()
            }
        assertNotNull(exception.cause)
    }
}
