package com.kafka.observatory

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaObservatoryApplication

fun main(args: Array<String>) {
    try {
        val dotenv = Dotenv.configure().ignoreIfMissing().load()
        dotenv.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    } catch (e: Exception) {
        // Ignore if .env cannot be loaded
    }
    runApplication<KafkaObservatoryApplication>(*args)
}
