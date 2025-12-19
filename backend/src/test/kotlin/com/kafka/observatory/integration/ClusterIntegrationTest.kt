package com.kafka.observatory.integration

import com.kafka.observatory.core.ports.ClusterPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ClusterIntegrationTest {
    companion object {
        @Container
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

        @JvmStatic
        @DynamicPropertySource
        fun registerKafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("KAFKA_BROKERS") { kafka.bootstrapServers }
        }
    }

    @Autowired
    private lateinit var clusterPort: ClusterPort

    @Test
    fun `should connect to kafka and retrieve cluster info`() {
        // This test verifies that the full stack from Adapter -> KafkaConnectionManager -> Kafka works.
        val clusterInfo = clusterPort.getClusterInfo()

        assertNotNull(clusterInfo)
        assertNotNull(clusterInfo.clusterId)
        assertEquals(1, clusterInfo.brokers.size)
        // Note: KafkaContainer usually exposes mapped ports, so we assert the number of brokers is correct.
    }
}
