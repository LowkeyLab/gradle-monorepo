package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.TestContainersConfig
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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

    @Test
    fun `can add a player to a game`() {
        val sut = GameService(gameRepository)
        val game = sut.new()
        val player = Player("Alice")

        sut.addPlayer(game, player)

        game.numberOfPlayers shouldBe 1
    }

    @Test
    fun `can start a game`() {
        val sut = GameService(gameRepository)
        val game = sut.new()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(game, alice)
        sut.addPlayer(game, bob)

        sut.start(game)

        game.currentRound shouldBe 1
    }

    @Test
    fun `can add a guess to a game`() {
        val sut = GameService(gameRepository)
        val game = sut.new()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(game, alice)
        sut.addPlayer(game, bob)
        sut.start(game)

        sut.addGuess(game, alice, "apple")

        game.guessesForRound(1)[alice] shouldBe "apple"
    }
}
