package com.kafka.observatory.adapters.web.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/api/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "app" to "kafka-observatory",
            "version" to "0.0.1",
        )
    }
}
