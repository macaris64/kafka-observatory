package com.kafka.observatory.adapters.kafka.consumer

import com.kafka.observatory.core.model.ConsumeSession
import com.kafka.observatory.core.model.ConsumeSessionState
import com.kafka.observatory.core.model.ConsumedMessage
import com.kafka.observatory.core.session.ConsumeSessionRegistry
import com.kafka.observatory.ports.kafka.KafkaConsumePort
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class KafkaConsumeAdapter(
    private val factory: KafkaConsumerFactory,
    private val registry: ConsumeSessionRegistry,
) : KafkaConsumePort {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    // Track active consumers to allow wakeup/close
    private val activeConsumers = ConcurrentHashMap<String, KafkaConsumer<String, String>>()

    override fun startConsumption(
        session: ConsumeSession,
        onMessage: (ConsumedMessage) -> Unit,
    ) {
        // Create consumer
        val consumer =
            try {
                factory.createConsumer(session.groupId, session.from)
            } catch (e: Exception) {
                logger.error("Failed to create consumer for session ${session.id}", e)
                throw e
            }

        activeConsumers[session.id] = consumer

        executor.submit {
            pollLoop(session, consumer, onMessage)
        }
        logger.info("Started consumption for session ${session.id} on topic ${session.topic}")
    }

    override fun stopConsumption(sessionId: String) {
        val consumer = activeConsumers[sessionId]
        if (consumer != null) {
            logger.info("Stopping consumption for session $sessionId")
            consumer.wakeup() // Interrupt poll
            // The loop will handle close and extraction from map
        } else {
            logger.warn("Attempted to stop unknown or already stopped session $sessionId")
        }
    }

    override fun pauseConsumption(sessionId: String) {
        val consumer = activeConsumers[sessionId]
        if (consumer != null) {
            logger.info("Triggering pause for session $sessionId")
            consumer.wakeup() // Wake up to react to state change
        }
    }

    override fun resumeConsumption(sessionId: String) {
        val consumer = activeConsumers[sessionId]
        if (consumer != null) {
            logger.info("Triggering resume for session $sessionId")
            consumer.wakeup() // Wake up to react to state change
        }
    }

    private fun pollLoop(
        session: ConsumeSession,
        consumer: KafkaConsumer<String, String>,
        onMessage: (ConsumedMessage) -> Unit,
    ) {
        try {
            consumer.subscribe(listOf(session.topic))

            var isPaused = false
            while (true) {
                try {
                    // Check state from registry
                    val currentSession = registry.getSession(session.id)
                    if (currentSession == null || currentSession.state == ConsumeSessionState.STOPPED) {
                        break
                    }

                    if (currentSession.state == ConsumeSessionState.PAUSED) {
                        if (!isPaused) {
                            logger.info("Pausing Kafka consumer for session ${session.id}")
                            consumer.pause(consumer.assignment())
                            isPaused = true
                        }
                    } else if (currentSession.state == ConsumeSessionState.RUNNING) {
                        if (isPaused) {
                            logger.info("Resuming Kafka consumer for session ${session.id}")
                            consumer.resume(consumer.assignment())
                            isPaused = false
                        }
                    }

                    val records = consumer.poll(Duration.ofMillis(500))

                    for (record in records) {
                        val message =
                            ConsumedMessage(
                                topic = record.topic(),
                                partition = record.partition(),
                                offset = record.offset(),
                                timestamp = record.timestamp(),
                                key = record.key(),
                                value = record.value(),
                                headers = record.headers().associate { it.key() to String(it.value()) },
                            )
                        onMessage(message)
                    }
                } catch (e: WakeupException) {
                    logger.info("Consumer woken up for session ${session.id} - checking state")
                    // No-op, the next iteration will check the state and either continue, pause/resume, or break
                }
            }
        } catch (e: Exception) {
            logger.error("Error in poll loop for session ${session.id}", e)
            registry.updateState(session.id, ConsumeSessionState.ERROR)
        } finally {
            try {
                consumer.close()
            } catch (e: Exception) {
                logger.error("Error closing consumer for session ${session.id}", e)
            }
            activeConsumers.remove(session.id)
            logger.info("Consumer closed for session ${session.id}")
        }
    }
}
