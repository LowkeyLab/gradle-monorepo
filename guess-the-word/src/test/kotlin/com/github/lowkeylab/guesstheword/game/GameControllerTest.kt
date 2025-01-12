package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.TestContainersConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
    fun `can create a new game`() {
        assertThat(
            mvc
                .post()
                .uri("/games"),
        ).hasStatus2xxSuccessful()
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
}
