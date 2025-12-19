package com.kafka.observatory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaObservatoryApplication

fun main(args: Array<String>) {
    runApplication<KafkaObservatoryApplication>(*args)
}
