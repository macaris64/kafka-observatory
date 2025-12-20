package com.kafka.observatory.core.session

import com.kafka.observatory.core.model.ConsumeSession
import com.kafka.observatory.core.model.ConsumeSessionState
import com.kafka.observatory.core.model.ConsumedMessage
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe registry for managing active consume sessions and their buffered messages.
 * Enforces per-session buffer limits.
 */
class ConsumeSessionRegistry {
    private val sessions = ConcurrentHashMap<String, ConsumeSession>()

    // Using simple synchronization on the Deque for bound enforcement,
    // or we can use a lock object per session.
    // For simplicity and correctness with bound check, we will sync on a dedicated lock map or the buffer itself.
    private val buffers = ConcurrentHashMap<String, ArrayDeque<ConsumedMessage>>()
    private val bufferLocks = ConcurrentHashMap<String, Any>()

    fun register(session: ConsumeSession) {
        sessions[session.id] = session
        buffers[session.id] = ArrayDeque()
        bufferLocks[session.id] = Any()
    }

    fun getSession(sessionId: String): ConsumeSession? {
        return sessions[sessionId]
    }

    fun removeSession(sessionId: String) {
        sessions.remove(sessionId)
        buffers.remove(sessionId)
        bufferLocks.remove(sessionId)
    }

    fun getAllSessions(): List<ConsumeSession> {
        return sessions.values.toList()
    }

    fun getBufferSize(sessionId: String): Int {
        return buffers[sessionId]?.size ?: 0
    }

    fun updateState(
        sessionId: String,
        state: ConsumeSessionState,
    ) {
        sessions.computeIfPresent(sessionId) { _, session ->
            session.copy(state = state)
        }
    }

    fun addMessage(
        sessionId: String,
        message: ConsumedMessage,
    ) {
        val lock = bufferLocks[sessionId] ?: return
        val buffer = buffers[sessionId] ?: return

        synchronized(lock) {
            if (buffer.size >= (sessions[sessionId]?.maxBufferSize ?: 0)) {
                // Remove oldest (first) to make room for new
                buffer.removeFirst()
            }
            buffer.addLast(message)
        }

        // Update lastConsumedAt
        sessions.computeIfPresent(sessionId) { _, session ->
            session.copy(lastConsumedAt = java.time.Instant.ofEpochMilli(message.timestamp))
        }
    }

    fun getMessages(
        sessionId: String,
        limit: Int = 100,
    ): List<ConsumedMessage> {
        val lock = bufferLocks[sessionId] ?: return emptyList()
        val buffer = buffers[sessionId] ?: return emptyList()

        // Prompt: "Return the newest messages first" => Last in, First out for viewing?
        // Usually logs/chats show newest at bottom. But "newest messages first" implies desc order.
        // We buffer in arrival order (append to end).
        // If we want newest first, we iterate from end.

        synchronized(lock) {
            val result = ArrayList<ConsumedMessage>(limit.coerceAtMost(buffer.size))
            val iterator = buffer.descendingIterator()
            var count = 0
            while (iterator.hasNext() && count < limit) {
                result.add(iterator.next())
                count++
            }
            return result
        }
    }
}
