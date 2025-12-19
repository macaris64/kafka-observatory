package com.kafka.observatory.adapters.web.rest

import com.kafka.observatory.core.domain.BrokerInfo
import com.kafka.observatory.core.domain.ClusterInfo
import com.kafka.observatory.core.exceptions.ClusterConnectivityException
import com.kafka.observatory.core.ports.ClusterPort
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ClusterController::class)
class ClusterControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var clusterPort: ClusterPort

    @Test
    fun `GET api_cluster should return cluster info`() {
        val clusterInfo =
            ClusterInfo(
                clusterId = "test-cluster",
                brokers = listOf(BrokerInfo(1, "localhost", 9092)),
            )
        `when`(clusterPort.getClusterInfo()).thenReturn(clusterInfo)

        mockMvc.perform(get("/api/cluster"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.clusterId").value("test-cluster"))
            .andExpect(jsonPath("$.data.brokers[0].id").value(1))
    }

    @Test
    fun `GET api_cluster should return 503 on ClusterConnectivityException`() {
        `when`(clusterPort.getClusterInfo()).thenThrow(ClusterConnectivityException("Kafka down"))

        mockMvc.perform(get("/api/cluster"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.error").value("Cluster Connectivity Error"))
    }
}
