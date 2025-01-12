package com.github.lowkeylab.guesstheword.game

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GameTest {
    @Test
    fun `can add two players to a game`() {
        val sut = Game()

        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))

        sut.numberOfPlayers shouldBe 2
    }

    @Test
    fun `cannot add more than two players to a game`() {
        val sut = Game()

        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))

        shouldThrow<IllegalStateException> {
            sut.addPlayer(Player("Charlie"))
        }
    }

    @Test
    fun `cannot start a game with fewer than two players`() {
        val sut = Game()

        sut.addPlayer(Player("Alice"))

        shouldThrow<IllegalStateException> {
            sut.start()
        }
    }

    @Test
    fun `can start a game with two players`() {
        val sut = Game()

        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))

        shouldNotThrowAny {
            sut.start()
        }
    }

    @Test
    fun `cannot add a player to a started game`() {
        val sut = Game()
        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))
        sut.start()

        shouldThrow<IllegalStateException> {
            sut.addPlayer(Player("Charlie"))
        }
    }

    @Test
    fun `a game starts with round 1`() {
        val sut = Game()
        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))

        sut.start()

        sut.currentRound shouldBe 1
    }

    @Test
    fun `can add a guess to a round`() {
        val sut = Game()
        val alice = Player("Alice")
        sut.addPlayer(alice)
        sut.addPlayer(Player("Bob"))
        sut.start()

        sut.addGuess(alice, "word")

        sut.guessesForRound(1)[alice] shouldBe "word"
    }

    @Test
    fun `after both players guess, a new round is started`() {
        val sut = Game()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(alice)
        sut.addPlayer(bob)
        sut.start()
        sut.addGuess(alice, "word")
        sut.addGuess(bob, "word")

        sut.currentRound shouldBe 2
    }
}
