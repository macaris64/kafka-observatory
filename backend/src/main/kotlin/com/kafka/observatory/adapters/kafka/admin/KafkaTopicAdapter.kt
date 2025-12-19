package com.kafka.observatory.adapters.kafka.admin

import com.kafka.observatory.adapters.kafka.KafkaConnectionManager
import com.kafka.observatory.core.model.TopicInfo
import com.kafka.observatory.ports.kafka.TopicPort
import org.apache.kafka.common.KafkaException
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutionException

@Component
class KafkaTopicAdapter(
    private val kafkaConnectionManager: KafkaConnectionManager,
) : TopicPort {
    override fun listTopics(): List<TopicInfo> {
        try {
            val adminClient = kafkaConnectionManager.getAdminClient()

            // listTopics() only returns names, we need describeTopics() for partitions
            val listTopicsResult = adminClient.listTopics()
            val names = listTopicsResult.names().get()

            if (names.isEmpty()) {
                return emptyList()
            }

            val describeTopicsResult = adminClient.describeTopics(names)
            val topicDescriptions = describeTopicsResult.allTopicNames().get()

            return topicDescriptions.map { (_, description) ->
                TopicInfo(
                    name = description.name(),
                    partitionCount = description.partitions().size,
                    // replication factor is usually per partition, but we can take the first one generic
                    // or average. For simplicity and since topics usually have uniform replication:
                    replicationFactor = description.partitions().firstOrNull()?.replicas()?.size ?: 0,
                )
            }.sortedBy { it.name }
        } catch (e: ExecutionException) {
            // Unpack ExecutionException
            val cause = e.cause
            if (cause is KafkaException) {
                throw RuntimeException("Kafka operation failed: ${cause.message}", cause)
            } else {
                throw RuntimeException("Unexpected error during topic listing: ${e.message}", e)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("Interrupted while listing topics", e)
        } catch (e: Exception) {
            throw RuntimeException("Failed to list topics: ${e.message}", e)
        }
    }
}
