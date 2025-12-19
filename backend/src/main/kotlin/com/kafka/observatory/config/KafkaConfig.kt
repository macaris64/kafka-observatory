package com.kafka.observatory.config

import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
class KafkaConfig(
    @Value("\${spring.profiles.active:default}") private val activeProfile: String,
) {
    @Value("\${KAFKA_BROKERS:}")
    private lateinit var brokers: String

    @Value("\${KAFKA_TLS_ENABLED:false}")
    private var tlsEnabled: Boolean = false

    @Value("\${KAFKA_TLS_CA_FILE:}")
    private lateinit var tlsCaFile: String

    @Value("\${KAFKA_TLS_CERT_FILE:}")
    private lateinit var tlsCertFile: String

    @Value("\${KAFKA_TLS_KEY_FILE:}")
    private lateinit var tlsKeyFile: String

    @Value("\${KAFKA_SASL_ENABLED:false}")
    private var saslEnabled: Boolean = false

    @Value("\${KAFKA_SASL_MECHANISM:PLAIN}")
    private lateinit var saslMechanism: String

    @Value("\${KAFKA_SASL_USERNAME:}")
    private lateinit var saslUsername: String

    @Value("\${KAFKA_SASL_PASSWORD:}")
    private lateinit var saslPassword: String

    @PostConstruct
    fun logConfig() {
        println("Kafka Observatory started with profile: $activeProfile")
    }

    fun buildCommonProperties(): Properties {
        if (brokers.isBlank()) {
            throw IllegalStateException("KAFKA_BROKERS environment variable is not set")
        }

        val props = Properties()
        props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = brokers

        val protocol =
            when {
                tlsEnabled && saslEnabled -> "SASL_SSL"
                tlsEnabled -> "SSL"
                saslEnabled -> "SASL_PLAINTEXT"
                else -> "PLAINTEXT"
            }
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = protocol

        if (tlsEnabled) {
            if (tlsCaFile.isNotBlank()) {
                props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "PEM"
                props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = tlsCaFile
            }
            if (tlsCertFile.isNotBlank() && tlsKeyFile.isNotBlank()) {
                props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PEM"
                props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = tlsCertFile
                props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = ""
            }
        }

        if (saslEnabled) {
            props[SaslConfigs.SASL_MECHANISM] = saslMechanism
            val loginModule =
                if (saslMechanism.startsWith("SCRAM")) {
                    "org.apache.kafka.common.security.scram.ScramLoginModule"
                } else {
                    "org.apache.kafka.common.security.plain.PlainLoginModule"
                }
            props[SaslConfigs.SASL_JAAS_CONFIG] = "$loginModule required username=\"$saslUsername\" password=\"$saslPassword\";"
        }

        return props
    }
}
