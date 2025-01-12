package com.github.lowkeylab.guesstheword.game

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Game(
    private val players: MutableList<Player> = mutableListOf(),
    @Id val id: String? = null,
) {
    val numberOfPlayers: Int
        get() = players.size

    fun addPlayer(player: Player) {
        check(players.size < 2) { "Game is full" }
        players.add(player)
    }
}

class Player(
    val name: String,
)
