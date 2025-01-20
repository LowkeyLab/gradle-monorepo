package com.github.lowkeylab.guesstheword.game.websocket

import com.github.lowkeylab.guesstheword.game.Game
import com.github.lowkeylab.guesstheword.game.GameService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller

@Controller
class GameWebSocketController(
    private val gameService: GameService,
) {
    private val games: MutableMap<String, Game> = mutableMapOf()

    @MessageMapping("/game/new")
    fun newGame(
        @Payload message: CreateGameMessage,
    ): GameCreatedMessage {
        val game = gameService.new()
        game.addPlayer(message.player)
        games[game.id!!] = game
        return GameCreatedMessage(game.id)
    }

    @MessageMapping("/game/{id}/addPlayer")
    fun addPlayer(
        @Payload message: AddPlayerMessage,
        @DestinationVariable gameId: String,
    ): PlayerAddedMessage {
        val game = games[message.gameId] ?: throw IllegalArgumentException("Game not found")
        game.addPlayer(message.player)
        return PlayerAddedMessage(game.id!!, message.player)
    }

    @MessageMapping("/game/{id}/addGuess")
    fun addGuess(
        @Payload message: AddGuessMessage,
    ): GuessAddedMessage {
        val game = games[message.gameId] ?: throw IllegalArgumentException("Game not found")
        game.addGuess(message.player, message.guess)
        return GuessAddedMessage(game.id!!, message.player)
    }
}
