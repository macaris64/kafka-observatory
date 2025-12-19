package com.kafka.observatory.adapters.kafka.admin

import com.kafka.observatory.adapters.kafka.KafkaConnectionManager
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.DescribeTopicsResult
import org.apache.kafka.clients.admin.ListTopicsResult
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.Node
import org.apache.kafka.common.TopicPartitionInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.ExecutionException

class KafkaTopicAdapterTest {
    private lateinit var kafkaConnectionManager: KafkaConnectionManager
    private lateinit var adminClient: AdminClient
    private lateinit var adapter: KafkaTopicAdapter

    @BeforeEach
    fun setUp() {
        kafkaConnectionManager = mock(KafkaConnectionManager::class.java)
        adminClient = mock(AdminClient::class.java)
        `when`(kafkaConnectionManager.getAdminClient()).thenReturn(adminClient)
        adapter = KafkaTopicAdapter(kafkaConnectionManager)
    }

    @Test
    fun `should list topics with correct metadata`() {
        // Mock listTopics
        val listTopicsResult = mock(ListTopicsResult::class.java)
        val topicNames = setOf("topic-1", "topic-2")
        `when`(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(topicNames))
        `when`(adminClient.listTopics()).thenReturn(listTopicsResult)

        // Mock describeTopics
        val describeTopicsResult = mock(DescribeTopicsResult::class.java)

        val node = Node(1, "host", 9092)
        val partitionInfo1 = TopicPartitionInfo(0, node, listOf(node, node), listOf(node, node)) // rep factor 2
        val description1 = TopicDescription("topic-1", false, listOf(partitionInfo1, partitionInfo1, partitionInfo1)) // 3 partitions

        val partitionInfo2 = TopicPartitionInfo(0, node, listOf(node), listOf(node)) // rep factor 1
        val description2 = TopicDescription("topic-2", false, listOf(partitionInfo2)) // 1 partition

        val descriptions =
            mapOf(
                "topic-1" to description1,
                "topic-2" to description2,
            )

        `when`(describeTopicsResult.allTopicNames()).thenReturn(KafkaFuture.completedFuture(descriptions))
        `when`(adminClient.describeTopics(topicNames)).thenReturn(describeTopicsResult)

        // Execute
        val result = adapter.listTopics()

        // Verify
        assertEquals(2, result.size)

        val t1 = result.find { it.name == "topic-1" }
        assertEquals(3, t1?.partitionCount)
        assertEquals(2, t1?.replicationFactor)

        val t2 = result.find { it.name == "topic-2" }
        assertEquals(1, t2?.partitionCount)
        assertEquals(1, t2?.replicationFactor)
    }

    @Test
    fun `should handle empty topic list`() {
        val listTopicsResult = mock(ListTopicsResult::class.java)
        `when`(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(emptySet()))
        `when`(adminClient.listTopics()).thenReturn(listTopicsResult)

        val result = adapter.listTopics()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should wrap Kafka exceptions`() {
        val listTopicsResult = mock(ListTopicsResult::class.java)

        // Mock the future to throw ExecutionException when get() is called
        val future = mock(KafkaFuture::class.java) as KafkaFuture<Set<String>>
        `when`(future.get()).thenThrow(ExecutionException(KafkaException("Timeout")))

        `when`(listTopicsResult.names()).thenReturn(future)
        `when`(adminClient.listTopics()).thenReturn(listTopicsResult)

        assertThrows(RuntimeException::class.java) {
            adapter.listTopics()
        }
    }
}
