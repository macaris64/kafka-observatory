package com.kafka.observatory.core.ports

import com.kafka.observatory.core.domain.ClusterInfo

interface ClusterPort {
    fun getClusterInfo(): ClusterInfo
}
