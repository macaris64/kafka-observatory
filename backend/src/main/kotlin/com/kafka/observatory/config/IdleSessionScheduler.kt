package com.kafka.observatory.config

import com.kafka.observatory.core.service.ConsumeSessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration

@Configuration
@EnableScheduling
class IdleSessionScheduler(
    private val service: ConsumeSessionService,
    @Value("\${app.session.idle-timeout:5m}")
    private val idleTimeout: Duration,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "PT1M")
    fun checkIdleSessions() {
        logger.debug("Checking for idle consume sessions (timeout: {})", idleTimeout)
        service.checkIdleSessions(idleTimeout)
    }
}
