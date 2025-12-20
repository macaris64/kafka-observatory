package com.kafka.observatory.ports.kafka

import com.kafka.observatory.core.model.ProduceRequest
import com.kafka.observatory.core.model.ProduceResponse

interface KafkaProducerPort {
    fun produce(request: ProduceRequest): ProduceResponse
}
