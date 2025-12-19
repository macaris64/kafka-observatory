package com.kafka.observatory.adapters.kafka

import com.kafka.observatory.core.domain.BrokerInfo
import com.kafka.observatory.core.domain.ClusterInfo
import com.kafka.observatory.core.exceptions.ClusterConnectivityException
import com.kafka.observatory.core.ports.ClusterPort
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutionException

@Component
class KafkaClusterAdapter(
    private val connectionManager: KafkaConnectionManager,
) : ClusterPort {
    override fun getClusterInfo(): ClusterInfo {
        val adminClient = connectionManager.getAdminClient()
        try {
            val clusterResult = adminClient.describeCluster()
            val clusterId = clusterResult.clusterId().get()
            val nodes = clusterResult.nodes().get()

            val brokers =
                nodes.map { node ->
                    BrokerInfo(
                        id = node.id(),
                        host = node.host(),
                        port = node.port(),
                    )
                }.sortedBy { it.id }

            return ClusterInfo(
                clusterId = clusterId,
                brokers = brokers,
            )
        } catch (e: ExecutionException) {
            throw ClusterConnectivityException("Failed to fetch cluster info: " + (e.cause?.message ?: e.message), e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ClusterConnectivityException("Interrupted while fetching cluster info", e)
        }
    }
}
