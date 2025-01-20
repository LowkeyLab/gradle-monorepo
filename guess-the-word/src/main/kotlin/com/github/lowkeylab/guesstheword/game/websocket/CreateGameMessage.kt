package com.github.lowkeylab.guesstheword.game.websocket

import com.github.lowkeylab.guesstheword.game.Player

data class CreateGameMessage(
    val player: Player,
)

data class GameCreatedMessage(
    val gameId: String,
)
