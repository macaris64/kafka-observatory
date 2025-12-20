package com.kafka.observatory.core.model

data class ProduceRequest(
    val topic: String,
    val key: String? = null,
    val value: String,
    val partition: Int? = null,
    val headers: Map<String, String>? = emptyMap(),
)
