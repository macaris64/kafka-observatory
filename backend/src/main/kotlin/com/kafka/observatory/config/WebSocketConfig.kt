package com.kafka.observatory.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kafka.observatory.adapters.web.websocket.SessionWebSocketHandler
import com.kafka.observatory.core.service.ConsumeSessionService
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val sessionService: ConsumeSessionService,
    private val objectMapper: ObjectMapper,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        // We use a wildcard to capture the sessionId in the handler
        registry.addHandler(SessionWebSocketHandler(sessionService, objectMapper), "/ws/consume-sessions/*")
            .setAllowedOrigins("*")
    }
}
