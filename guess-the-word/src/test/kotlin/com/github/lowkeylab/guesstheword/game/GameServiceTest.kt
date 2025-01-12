package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.player.Player
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

    @Test
    fun `service can save game with players`() {
        val sut = gameService
        val players =
            mutableListOf(
                Player(name = "Alice"),
                Player(name = "Bob"),
            )
        val game = Game(players = players)

        val result = sut.save(game)

        result.id.shouldNotBeNull()
    }
}
