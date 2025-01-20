package com.github.lowkeylab.guesstheword.game.websocket

import com.github.lowkeylab.guesstheword.game.Player

data class CreateGameMessage(
    val player: Player,
)

data class GameCreatedMessage(
    val gameId: String,
)

data class AddPlayerMessage(
    val gameId: String,
    val player: Player,
)

data class PlayerAddedMessage(
    val gameId: String,
    val player: Player,
)

data class AddGuessMessage(
    val gameId: String,
    val player: Player,
    val guess: String,
)

data class GuessAddedMessage(
    val gameId: String,
    val player: Player,
)
