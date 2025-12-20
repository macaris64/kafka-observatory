package com.kafka.observatory.core.service

import com.kafka.observatory.core.model.ConsumeFrom
import com.kafka.observatory.core.model.ConsumeSessionState
import com.kafka.observatory.core.session.ConsumeSessionRegistry
import com.kafka.observatory.ports.kafka.KafkaConsumePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ConsumeSessionServiceTest {
    private val registry = ConsumeSessionRegistry()
    private val kafkaPort = Mockito.mock(KafkaConsumePort::class.java)
    private val service = ConsumeSessionService(registry, kafkaPort)

    @Test
    fun `should start session`() {
        val session = service.startSession("topic1", "group1", ConsumeFrom.LATEST, 100)

        assertNotNull(session.id)
        assertEquals("topic1", session.topic)
        assertEquals("group1", session.groupId)
        assertEquals(ConsumeSessionState.RUNNING, session.state)

        assertNotNull(registry.getSession(session.id))
        Mockito.verify(kafkaPort).startConsumption(session)
    }

    @Test
    fun `should stop session`() {
        val session = service.startSession("topic1", null, ConsumeFrom.EARLIEST, 100)

        service.stopSession(session.id)

        assertEquals(ConsumeSessionState.STOPPED, registry.getSession(session.id)?.state)
        Mockito.verify(kafkaPort).stopConsumption(session.id)
    }

    @Test
    fun `should throw if stopping unknown session`() {
        assertThrows(NoSuchElementException::class.java) {
            service.stopSession("unknown")
        }
    }

    @Test
    fun `should pause session`() {
        val session = service.startSession("topic1", "group1", ConsumeFrom.LATEST, 100)

        service.pauseSession(session.id)

        assertEquals(ConsumeSessionState.PAUSED, registry.getSession(session.id)?.state)
        Mockito.verify(kafkaPort).pauseConsumption(session.id)
    }

    @Test
    fun `should resume session`() {
        val session = service.startSession("topic1", "group1", ConsumeFrom.LATEST, 100)
        service.pauseSession(session.id)

        service.resumeSession(session.id)

        assertEquals(ConsumeSessionState.RUNNING, registry.getSession(session.id)?.state)
        Mockito.verify(kafkaPort).resumeConsumption(session.id)
    }

    @Test
    fun `should get session status`() {
        val session = service.startSession("topic1", "group1", ConsumeFrom.LATEST, 100)

        val status = service.getStatus(session.id)

        assertEquals(session.id, status.sessionId)
        assertEquals("topic1", status.topic)
        assertEquals(ConsumeSessionState.RUNNING, status.state)
        assertEquals(0, status.bufferSize)
    }

    @Test
    fun `should auto-stop idle sessions`() {
        val session = service.startSession("topic1", "group1", ConsumeFrom.LATEST, 100)
        // Last activity (createdAt) is now.

        // Check with 10 min timeout, should NOT stop (fresh)
        service.checkIdleSessions(java.time.Duration.ofMinutes(10))
        assertEquals(ConsumeSessionState.RUNNING, registry.getSession(session.id)?.state)

        // Artificially age the session? Registry doesn't support setting createdAt.
        // But we can update the state to STOPPED manually or mock?
        // Actually, we can't easily age createdAt without reflection or changing model.
        // Let's change the test to use a very small timeout and wait a bit, or just trust the logic if it's simple.
        // Better: Wait 1s and use 500ms timeout.
        Thread.sleep(1000)
        service.checkIdleSessions(java.time.Duration.ofMillis(500))
        assertEquals(ConsumeSessionState.STOPPED, registry.getSession(session.id)?.state)
    }
}
