package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.TestContainersConfig
import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun `can create a new game`() {
        assertThat(
            mvc
                .post()
                .uri("/games"),
        ).hasStatus2xxSuccessful()
    }
}
