package com.kafka.observatory.adapters.web.rest

import com.kafka.observatory.core.model.ProduceRequest
import com.kafka.observatory.ports.kafka.KafkaProducerPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/produce")
class ProduceController(
    private val producerPort: KafkaProducerPort,
) {
    @PostMapping
    fun produce(
        @RequestBody request: ProduceRequest,
    ): ResponseEntity<Map<String, Any>> {
        val response = producerPort.produce(request)
        return ResponseEntity.ok(mapOf("data" to response))
    }
}
