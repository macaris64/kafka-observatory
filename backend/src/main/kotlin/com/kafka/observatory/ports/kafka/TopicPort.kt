package com.kafka.observatory.ports.kafka

import com.kafka.observatory.core.model.TopicInfo

interface TopicPort {
    fun listTopics(): List<TopicInfo>
}
