package com.kafka.observatory

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaObservatoryApplication

fun main(args: Array<String>) {
    try {
        try {
            val parentDotenv = Dotenv.configure().directory("..").ignoreIfMissing().load()
            parentDotenv.entries().forEach { System.setProperty(it.key, it.value) }
            val dotenv = Dotenv.configure().ignoreIfMissing().load()
            dotenv.entries().forEach { System.setProperty(it.key, it.value) }
        } catch (e: Exception) {
            // Ignore if .env cannot be loaded
        }
    } catch (e: Exception) {
        // Ignore if .env cannot be loaded
    }
    runApplication<KafkaObservatoryApplication>(*args)
}
