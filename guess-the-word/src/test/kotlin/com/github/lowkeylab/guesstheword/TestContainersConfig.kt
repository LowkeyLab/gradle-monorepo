package com.github.lowkeylab.guesstheword

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MongoDBContainer

@TestConfiguration
class TestContainersConfig {
    @Bean
    @ServiceConnection
    fun mongoContainer(): MongoDBContainer = MongoDBContainer("mongo:7.0.16")
}
