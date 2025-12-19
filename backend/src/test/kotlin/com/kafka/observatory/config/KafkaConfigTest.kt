package com.kafka.observatory.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class KafkaConfigTest {
    @Test
    fun `buildCommonProperties should throw exception when brokers are missing`() {
        val config = KafkaConfig("test")
        ReflectionTestUtils.setField(config, "brokers", "")

        assertThrows(IllegalStateException::class.java) {
            config.buildCommonProperties()
        }
    }

    @Test
    fun `buildCommonProperties should set correct protocol for PLAINTEXT`() {
        val config = KafkaConfig("test")
        ReflectionTestUtils.setField(config, "brokers", "localhost:9092")
        ReflectionTestUtils.setField(config, "tlsEnabled", false)
        ReflectionTestUtils.setField(config, "saslEnabled", false)

        val props = config.buildCommonProperties()
        assertEquals("localhost:9092", props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG])
        assertEquals("PLAINTEXT", props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG])
    }

    @Test
    fun `buildCommonProperties should set correct protocol for SSL`() {
        val config = KafkaConfig("test")
        ReflectionTestUtils.setField(config, "brokers", "localhost:9092")
        ReflectionTestUtils.setField(config, "tlsEnabled", true)
        ReflectionTestUtils.setField(config, "saslEnabled", false)
        ReflectionTestUtils.setField(config, "tlsCaFile", "/path/to/ca.pem")
        ReflectionTestUtils.setField(config, "tlsCertFile", "")
        ReflectionTestUtils.setField(config, "tlsKeyFile", "")

        val props = config.buildCommonProperties()
        assertEquals("SSL", props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG])
        assertEquals("PEM", props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG])
        assertEquals("/path/to/ca.pem", props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG])
    }

    @Test
    fun `buildCommonProperties should set correct protocol for SASL_PLAINTEXT`() {
        val config = KafkaConfig("test")
        ReflectionTestUtils.setField(config, "brokers", "localhost:9092")
        ReflectionTestUtils.setField(config, "tlsEnabled", false)
        ReflectionTestUtils.setField(config, "saslEnabled", true)
        ReflectionTestUtils.setField(config, "saslMechanism", "PLAIN")
        ReflectionTestUtils.setField(config, "saslUsername", "user")
        ReflectionTestUtils.setField(config, "saslPassword", "pass")

        val props = config.buildCommonProperties()
        assertEquals("SASL_PLAINTEXT", props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG])
        assertEquals("PLAIN", props[SaslConfigs.SASL_MECHANISM])
        assertTrue(props[SaslConfigs.SASL_JAAS_CONFIG].toString().contains("PlainLoginModule"))
        assertTrue(props[SaslConfigs.SASL_JAAS_CONFIG].toString().contains("username=\"user\""))
    }
}
