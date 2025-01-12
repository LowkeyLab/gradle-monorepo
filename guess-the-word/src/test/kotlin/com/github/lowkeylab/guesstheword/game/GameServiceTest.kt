package com.github.lowkeylab.guesstheword.game

import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestContainerConfiguration::class)
@SpringBootTest
class GameServiceTest {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gameRepository: GameRepository

    @BeforeEach
    fun setUp() {
        gameRepository.deleteAll()
    }

    @Test
    fun `service can create new game`() {
        val sut = gameService

        val result = sut.new()

        result.id.shouldNotBeNull()
    }
}
