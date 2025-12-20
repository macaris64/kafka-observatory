package com.kafka.observatory.core.model

import java.time.Instant

data class ConsumeSession(
    val id: String,
    val topic: String,
    val groupId: String,
    val from: ConsumeFrom,
    val maxBufferSize: Int,
    val state: ConsumeSessionState = ConsumeSessionState.RUNNING,
    val lastConsumedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
)
