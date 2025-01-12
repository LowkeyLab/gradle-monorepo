package com.github.lowkeylab.guesstheword.game

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Game(
    private val players: MutableCollection<Player> = mutableSetOf(),
    private val rounds: MutableList<Round> = mutableListOf(Round()),
    @Id val id: String? = null,
) {
    @Transient
    private var started: Boolean = false

    val currentRound
        get() = rounds.size

    val numberOfPlayers: Int
        get() = players.size

    fun addPlayer(player: Player) {
        check(players.size < 2) { "Game is full" }
        check(!players.contains(player)) { "Cannot add the same player twice" }
        players.add(player)
    }

    fun start() {
        check(players.size == 2) { "Game must have 2 players" }
        started = true
    }

    fun addGuess(
        player: Player,
        guess: String,
    ) {
        check(started) { "Game has not started" }
        val currentRound = rounds.last()
        check(currentRound.getGuessFor(player) == null) { "Player has already guessed" }
        currentRound.addGuess(player, guess)
        if (currentRound.numberOfGuesses() == 2) {
            rounds.add(Round())
        }
    }

    fun guessesForRound(round: Int): Map<Player, String> = rounds[round - 1].guesses
}

class Player(
    val name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}

class Round(
    @Transient
    val guesses: MutableMap<Player, String> = mutableMapOf(),
) {
    fun addGuess(
        player: Player,
        guess: String,
    ) {
        require(guess.isNotEmpty()) { "Guess cannot be empty" }
        guesses[player] = guess
    }

    fun getGuessFor(player: Player): String? = guesses[player]

    fun numberOfGuesses(): Int = guesses.size

    @Suppress("ktlint:standard:backing-property-naming")
    private val _guesses: Map<String, String>
        get() = guesses.map { (player, guess) -> player.name to guess }.toMap()
}
