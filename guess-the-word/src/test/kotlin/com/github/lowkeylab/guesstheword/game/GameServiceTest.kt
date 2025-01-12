package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.TestContainersConfig
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import

@DataMongoTest
@Import(TestContainersConfig::class)
class GameServiceTest {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Test
    fun `can create a new game`() {
        val sut = GameService(gameRepository)

        val output = sut.new()

        output.id.shouldNotBeNull()
    }
}
