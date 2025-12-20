package com.kafka.observatory.core.session

import com.kafka.observatory.core.model.ConsumeFrom
import com.kafka.observatory.core.model.ConsumeSession
import com.kafka.observatory.core.model.ConsumedMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ConsumeSessionRegistryTest {
    private val registry = ConsumeSessionRegistry()

    @Test
    fun `should register and retrieve session`() {
        val session = createSession()
        registry.register(session)

        val retrieved = registry.getSession(session.id)
        assertNotNull(retrieved)
        assertEquals(session, retrieved)
    }

    @Test
    fun `should buffer messages respecting max size`() {
        val session = createSession(maxSize = 5)
        registry.register(session)

        for (i in 1..10) {
            registry.addMessage(session.id, createMessage(i.toLong()))
        }

        val messages = registry.getMessages(session.id)
        assertEquals(5, messages.size)
        assertEquals(5, registry.getBufferSize(session.id))
        // Should have last 5 messages: 6, 7, 8, 9, 10
        // getMessages returns them in newest-first order (10, 9, 8, 7, 6)
        assertEquals(10L, messages[0].offset)
        assertEquals(6L, messages[4].offset)
    }

    @Test
    fun `should update lastConsumedAt when adding message`() {
        val session = createSession()
        registry.register(session)

        val timestamp = 123456789L
        registry.addMessage(session.id, createMessage(1L, timestamp))

        val updatedSession = registry.getSession(session.id)
        assertNotNull(updatedSession?.lastConsumedAt)
        assertEquals(timestamp, updatedSession?.lastConsumedAt?.toEpochMilli())
    }

    @Test
    fun `should return empty if session not found`() {
        assertTrue(registry.getMessages("unknown").isEmpty())
    }

    @Test
    fun `should limit returned messages`() {
        val session = createSession(maxSize = 10)
        registry.register(session)
        for (i in 1..10) {
            registry.addMessage(session.id, createMessage(i.toLong()))
        }

        val messages = registry.getMessages(session.id, limit = 3)
        assertEquals(3, messages.size)
        // Expect 8, 9, 10 if we want newest first?
        // Implementation:
        // val iterator = buffer.descendingIterator()
        // result.add(iterator.next())
        // So yes, it returns newest first (descending order).
        // Added 1..10. Buffer: [1,2,3...10].
        // Descending: 10, 9, 8...
        assertEquals(10L, messages[0].offset)
        assertEquals(9L, messages[1].offset)
        assertEquals(8L, messages[2].offset)
    }

    private fun createSession(maxSize: Int = 100) =
        ConsumeSession(
            id = UUID.randomUUID().toString(),
            topic = "test-topic",
            groupId = "g1",
            from = ConsumeFrom.LATEST,
            maxBufferSize = maxSize,
        )

    private fun createMessage(
        offset: Long,
        timestamp: Long = System.currentTimeMillis(),
    ) = ConsumedMessage(
        topic = "test-topic",
        partition = 0,
        offset = offset,
        timestamp = timestamp,
        key = "k",
        value = "v",
        headers = emptyMap(),
    )
}
