package com.kafka.observatory.core.model

import java.time.Instant

data class ConsumeSessionStatus(
    val sessionId: String,
    val topic: String,
    val state: ConsumeSessionState,
    val lastConsumedAt: Instant?,
    val bufferSize: Int,
)
