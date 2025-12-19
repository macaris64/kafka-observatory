package com.kafka.observatory.core.model

data class TopicInfo(
    val name: String,
    val partitionCount: Int,
    val replicationFactor: Int,
)
