package com.kafka.observatory.core.service

import com.kafka.observatory.core.model.ConsumeFrom
import com.kafka.observatory.core.model.ConsumeSessionState
import com.kafka.observatory.core.ports.messaging.SessionMessageBroadcaster
import com.kafka.observatory.core.session.ConsumeSessionRegistry
import com.kafka.observatory.ports.kafka.KafkaConsumePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito

class ConsumeSessionServiceTest {
    private val registry = ConsumeSessionRegistry()
    private val kafkaPort = Mockito.mock(KafkaConsumePort::class.java)
    private val broadcaster = Mockito.mock(SessionMessageBroadcaster::class.java)
    private val service = ConsumeSessionService(registry, kafkaPort, broadcaster)

    @Test
    fun `should start session`() {
        val session = service.startSession("topic1", "group1", ConsumeFrom.LATEST, 100)

        assertNotNull(session.id)
        assertEquals("topic1", session.topic)
        assertEquals("group1", session.groupId)
        assertEquals(ConsumeSessionState.RUNNING, session.state)

        assertNotNull(registry.getSession(session.id))
        Mockito.verify(kafkaPort).startConsumption(any(), any())
    }

    @Test
    fun `should stop session`() {
        val session = service.startSession("topic1", null, ConsumeFrom.EARLIEST, 100)

        service.stopSession(session.id)

        assertEquals(ConsumeSessionState.STOPPED, registry.getSession(session.id)?.state)
        Mockito.verify(kafkaPort).stopConsumption(session.id)
        Mockito.verify(broadcaster).closeSession(session.id)
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

        // Wait 1s and use 500ms timeout.
        Thread.sleep(1000)
        service.checkIdleSessions(java.time.Duration.ofMillis(500))
        assertEquals(ConsumeSessionState.STOPPED, registry.getSession(session.id)?.state)
        Mockito.verify(broadcaster).closeSession(session.id)
    }

    private fun <T> any(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }
}
