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
}
