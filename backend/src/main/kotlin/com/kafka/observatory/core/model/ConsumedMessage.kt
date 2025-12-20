package com.kafka.observatory.core.model

data class ConsumedMessage(
    val topic: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long,
    val key: String?,
    val value: String?,
    val headers: Map<String, String>,
)
