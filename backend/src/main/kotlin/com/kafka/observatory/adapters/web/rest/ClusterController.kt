package com.kafka.observatory.adapters.web.rest

import com.kafka.observatory.core.domain.ClusterInfo
import com.kafka.observatory.core.ports.ClusterPort
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cluster")
class ClusterController(
    private val clusterPort: ClusterPort,
) {
    @Operation(summary = "Get Kafka cluster information")
    @GetMapping
    fun getClusterInfo(): ResponseEntity<Map<String, ClusterInfo>> {
        val info = clusterPort.getClusterInfo()
        return ResponseEntity.ok(mapOf("data" to info))
    }
}
