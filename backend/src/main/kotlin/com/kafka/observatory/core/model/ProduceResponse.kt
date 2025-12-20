package com.kafka.observatory.core.model

data class ProduceResponse(
    val topic: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long,
)
