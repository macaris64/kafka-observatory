package com.kafka.observatory.adapters.web.rest

import com.kafka.observatory.core.model.TopicInfo
import com.kafka.observatory.ports.kafka.TopicPort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/topics")
class TopicController(
    private val topicPort: TopicPort,
) {
    @GetMapping
    fun listTopics(): TopicListResponse {
        val topics = topicPort.listTopics()
        return TopicListResponse(data = topics)
    }

    data class TopicListResponse(
        val data: List<TopicInfo>,
    )
}
