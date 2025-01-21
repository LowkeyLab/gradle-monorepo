package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.TestContainersConfig
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.assertj.MockMvcTester

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig::class)
class GameControllerTest {
    @Autowired
    private lateinit var mvc: MockMvcTester

    @Autowired
    private lateinit var gameRepository: GameRepository

    @BeforeEach
    fun setUp() {
        gameRepository.deleteAll()
    }

    @Test
    fun `can get a game`() {
        val savedGame = gameRepository.save(Game())

        assertThat(
            mvc
                .get()
                .uri("/games/${savedGame.id}"),
        ).hasStatus2xxSuccessful()
    }

    @Test
    fun `trying to find a non-existent game should return 404`() {
        assertThat(
            mvc
                .get()
                .uri("/games/123"),
        ).hasStatus(404)
    }

    @Nested
    inner class WebSocketTest {
        @Autowired
        private lateinit var sut: GameController

        @Test
        fun `can create a game`() {
            shouldNotThrowAny {
                sut.newGame(Player("Alice"))
            }
        }

        @Test
        fun `can add a player to a game`() {
            val game = sut.newGame(Player("Alice"))
            val newPlayer = Player("Bob")

            val result = sut.addPlayer(game, newPlayer)

            result shouldBe newPlayer
        }

        @Test
        fun `can start a game`() {
            val game = sut.newGame(Player("Alice"))
            sut.addPlayer(game, Player("Bob"))

            val result = sut.startGame(game)

            result.shouldBeTrue()
        }

        @Test
        fun `can add a guess to a game`() {
            val playerOne = Player("Alice")
            val guess = "test"
            val expected = GuessAddedMessage(playerOne, guess)
            val game = sut.newGame(playerOne)
            sut.addPlayer(game, Player("Bob"))
            sut.startGame(game)

            val result = sut.addGuess(game, AddGuessMessage(playerOne, guess))

            result shouldBe expected
        }
    }
}
