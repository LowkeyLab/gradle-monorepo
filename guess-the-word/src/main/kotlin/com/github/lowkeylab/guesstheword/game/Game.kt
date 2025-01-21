package com.github.lowkeylab.guesstheword.game

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Game(
    private val players: MutableCollection<Player> = mutableSetOf(),
    private val rounds: MutableList<Round> = mutableListOf(Round()),
    @Id val id: String? = null,
) {
    private var started: Boolean = false
    var ended: Boolean = false

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
        check(!ended) { "Game has ended" }
        val currentRound = rounds.last()
        check(currentRound.getGuessFor(player) == null) { "Player has already guessed" }
        currentRound.addGuess(player, guess)
        if (currentRound.numberOfGuesses() == 2) {
            if (currentRound.guesses.values
                    .distinct()
                    .size == 1
            ) {
                ended = true
            } else {
                rounds.add(Round())
            }
        }
    }

    fun guessesForRound(round: Int): Map<Player, String> = rounds[round - 1].guesses
}

data class Player(
    val name: String,
)

data class Round(
    private val _guesses: MutableMap<String, String> = mutableMapOf(),
) {
    fun addGuess(
        player: Player,
        guess: String,
    ) {
        require(guess.isNotEmpty()) { "Guess cannot be empty" }
        _guesses[player.name] = guess
    }

    fun getGuessFor(player: Player): String? = _guesses[player.name]

    fun numberOfGuesses(): Int = _guesses.size

    val guesses: Map<Player, String>
        get() = _guesses.mapKeys { Player(it.key) }
}
