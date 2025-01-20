package com.github.lowkeylab.guesstheword.game

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient

@SpringBootTest
class GameWebsocketControllerTest {
    private val client = WebSocketStompClient(StandardWebSocketClient())

    init {
        client.messageConverter = MappingJackson2MessageConverter()
    }

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `can create game`() {
    }
}

private class TestStompHandler
