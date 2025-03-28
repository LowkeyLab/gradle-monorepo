package com.github.lowkeylab.guesstheword.game

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
        sut.started shouldBe false
    }

    @Test
    fun `can start a game with two players`() {
        val sut = Game()

        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))
    }

    @Test
    fun `cannot add a player to a started game`() {
        val sut = Game()
        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))

        shouldThrow<IllegalStateException> {
            sut.addPlayer(Player("Charlie"))
        }
    }

    @Test
    fun `two players cannot have the same name`() {
        val sut = Game()
        sut.addPlayer(Player("Alice"))

        shouldThrow<IllegalStateException> {
            sut.addPlayer(Player("Alice"))
        }
    }

    @Test
    fun `a game starts with round 1`() {
        val sut = Game()
        sut.addPlayer(Player("Alice"))
        sut.addPlayer(Player("Bob"))

        sut.currentRound shouldBe 1
    }

    @Test
    fun `can add a guess to a round`() {
        val sut = Game()
        val alice = Player("Alice")
        sut.addPlayer(alice)
        sut.addPlayer(Player("Bob"))

        sut.addGuess(alice, "word")

        sut.guessesForRound(1)[alice] shouldBe "word"
    }

    @Test
    fun `cannot add a guess to a game that has not started`() {
        val sut = Game()
        val alice = Player("Alice")
        sut.addPlayer(alice)

        shouldThrow<IllegalStateException> {
            sut.addGuess(alice, "word")
        }
    }

    @Test
    fun `same player cannot guess twice in a row`() {
        val sut = Game()
        val alice = Player("Alice")
        sut.addPlayer(alice)
        sut.addPlayer(Player("Bob"))
        sut.addGuess(alice, "word")

        shouldThrow<IllegalStateException> {
            sut.addGuess(alice, "word")
        }
    }

    @Test
    fun `cannot make an empty guess`() {
        val sut = Game()
        val alice = Player("Alice")
        sut.addPlayer(alice)
        sut.addPlayer(Player("Bob"))

        shouldThrow<IllegalArgumentException> {
            sut.addGuess(alice, "")
        }
    }

    @Test
    fun `after both players guess different words, a new round is started`() {
        val sut = Game()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(alice)
        sut.addPlayer(bob)
        sut.addGuess(alice, "word")
        sut.addGuess(bob, "something")

        sut.currentRound shouldBe 2
    }

    @Test
    fun `after both players guess the same word, the game ends`() {
        val sut = Game()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(alice)
        sut.addPlayer(bob)
        sut.addGuess(alice, "word")
        sut.addGuess(bob, "word")

        sut.ended shouldBe true
    }

    @Test
    fun `after both players guess the same word, no more rounds are added`() {
        val sut = Game()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(alice)
        sut.addPlayer(bob)
        sut.addGuess(alice, "word")
        sut.addGuess(bob, "word")

        sut.currentRound shouldBe 1
    }

    @Test
    fun `after the game has ended, no more guesses can be added`() {
        val sut = Game()
        val alice = Player("Alice")
        val bob = Player("Bob")
        sut.addPlayer(alice)
        sut.addPlayer(bob)
        sut.addGuess(alice, "word")
        sut.addGuess(bob, "word")

        shouldThrow<IllegalStateException> {
            sut.addGuess(alice, "word")
        }
    }
}
