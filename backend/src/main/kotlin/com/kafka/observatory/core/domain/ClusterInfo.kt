package com.kafka.observatory.core.domain

data class ClusterInfo(
    val clusterId: String?,
    val brokers: List<BrokerInfo>,
)

data class BrokerInfo(
    val id: Int,
    val host: String,
    val port: Int,
)
