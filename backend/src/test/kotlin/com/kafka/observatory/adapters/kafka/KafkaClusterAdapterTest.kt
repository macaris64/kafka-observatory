package com.kafka.observatory.adapters.kafka

import com.kafka.observatory.core.exceptions.ClusterConnectivityException
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.DescribeClusterResult
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.ExecutionException

class KafkaClusterAdapterTest {
    private lateinit var connectionManager: KafkaConnectionManager
    private lateinit var adminClient: AdminClient
    private lateinit var adapter: KafkaClusterAdapter

    @BeforeEach
    fun setUp() {
        connectionManager = mock(KafkaConnectionManager::class.java)
        adminClient = mock(AdminClient::class.java)
        `when`(connectionManager.getAdminClient()).thenReturn(adminClient)
        adapter = KafkaClusterAdapter(connectionManager)
    }

    @Test
    fun `getClusterInfo should return correct cluster info`() {
        val result = mock(DescribeClusterResult::class.java)
        val clusterIdFuture = mock(KafkaFuture::class.java) as KafkaFuture<String>
        val nodesFuture = mock(KafkaFuture::class.java) as KafkaFuture<Collection<Node>>

        `when`(adminClient.describeCluster()).thenReturn(result)
        `when`(result.clusterId()).thenReturn(clusterIdFuture)
        `when`(result.nodes()).thenReturn(nodesFuture)

        `when`(clusterIdFuture.get()).thenReturn("test-cluster")
        val nodes =
            listOf(
                Node(1, "host1", 9092),
                Node(2, "host2", 9093),
            )
        `when`(nodesFuture.get()).thenReturn(nodes)

        val info = adapter.getClusterInfo()

        assertEquals("test-cluster", info.clusterId)
        assertEquals(2, info.brokers.size)
        assertEquals(1, info.brokers[0].id)
        assertEquals("host1", info.brokers[0].host)
        assertEquals(9092, info.brokers[0].port)
    }

    @Test
    fun `getClusterInfo should throw ClusterConnectivityException on ExecutionException`() {
        val result = mock(DescribeClusterResult::class.java)
        val clusterIdFuture = mock(KafkaFuture::class.java) as KafkaFuture<String>

        `when`(adminClient.describeCluster()).thenReturn(result)
        `when`(result.clusterId()).thenReturn(clusterIdFuture)
        `when`(clusterIdFuture.get()).thenThrow(ExecutionException("Kafka down", RuntimeException("Cause")))

        assertThrows(ClusterConnectivityException::class.java) {
            adapter.getClusterInfo()
        }
    }
}
