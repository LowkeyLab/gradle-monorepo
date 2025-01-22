package com.github.lowkeylab.guesstheword.game

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class GameMessage

data class GameEvent(
    val message: GameMessage,
)

data class GameStartedMessage(
    val id: String,
) : GameMessage()

data class AddPlayerMessage(
    val player: Player,
) : GameMessage()

data class PlayerAddedMessage(
    val player: Player,
) : GameMessage()

data class AddGuessMessage(
    val player: Player,
    val guess: String,
) : GameMessage()

data class GuessAddedMessage(
    val player: Player,
    val guess: String,
) : GameMessage()
