package com.github.lowkeylab.guesstheword.game

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class ClientGameMessage

data class AddPlayerMessage(
    val player: Player,
) : ClientGameMessage()

data class AddGuessMessage(
    val player: Player,
    val guess: String,
) : ClientGameMessage()

data class ClientGameEvent(
    val message: ClientGameMessage,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class ServerGameMessage

data class ServerGameEvent(
    val message: ServerGameMessage,
)

class GameStartedMessage : ServerGameMessage()

class GameEndedMessage : ServerGameMessage()

data class PlayerAddedMessage(
    val player: Player,
) : ServerGameMessage()

data class GuessAddedMessage(
    val player: Player,
    val guess: String,
) : ServerGameMessage()
