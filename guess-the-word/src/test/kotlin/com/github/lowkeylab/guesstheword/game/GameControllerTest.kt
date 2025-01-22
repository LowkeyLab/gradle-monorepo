package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.TestContainersConfig
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
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

    @Test
    fun `can create a game`() {
        assertThat(
            mvc
                .post()
                .uri("/games"),
        ).hasStatus2xxSuccessful().bodyJson().extractingPath("$.id").isNotNull()
    }

    @Nested
    inner class WebSocketTest {
        @Autowired
        private lateinit var sut: GameController

        @Test
        fun `can create a game`() {
            val result = sut.newGame()

            result.id.shouldNotBeNull()
        }

        @Test
        fun `can add a player to a game`() {
            val gameId = sut.newGame().id!!
            val gameEvent = ClientGameEvent(AddPlayerMessage(Player("Alice")))
            val expected = PlayerAddedMessage(Player("Alice"))
            val result = sut.processGameEvent(gameId, gameEvent)

            result.message
                .shouldBeTypeOf<PlayerAddedMessage>()
                .shouldBe(expected)
        }

        @Nested
        inner class GameStarted {
            private lateinit var gameId: String

            @BeforeEach
            fun setUp() {
                gameId = sut.newGame().id!!
                listOf(
                    ClientGameEvent(AddPlayerMessage(Player("Alice"))),
                    ClientGameEvent(AddPlayerMessage(Player("Bob"))),
                ).forEach {
                    sut.processGameEvent(gameId, it)
                }
            }

            @Test
            fun `can add a guess to a game`() {
                val expected = GuessAddedMessage(Player("Alice"), "word")
                val result = sut.processGameEvent(gameId, ClientGameEvent(AddGuessMessage(Player("Alice"), "word")))

                result.message
                    .shouldBeTypeOf<GuessAddedMessage>()
                    .shouldBe(expected)
            }
        }
    }
}
