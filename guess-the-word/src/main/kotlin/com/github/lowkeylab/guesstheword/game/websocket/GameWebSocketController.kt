package com.github.lowkeylab.guesstheword.game.websocket

import com.github.lowkeylab.guesstheword.game.Game
import com.github.lowkeylab.guesstheword.game.GameService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class GameWebSocketController(
    private val gameService: GameService,
) {
    private val games: MutableMap<String, Game> = mutableMapOf()

    @MessageMapping("/games/new")
    @SendTo("/games")
    fun newGame(
        @Payload message: CreateGameMessage,
    ): GameCreatedMessage {
        val game = gameService.new()
        game.addPlayer(message.player)
        games[game.id!!] = game
        return GameCreatedMessage(game.id)
    }

    @MessageMapping("/games/addPlayer")
    @SendTo("/games")
    fun addPlayer(
        @Payload message: AddPlayerMessage,
    ): PlayerAddedMessage {
        val game = games[message.gameId] ?: throw IllegalArgumentException("Game not found")
        game.addPlayer(message.player)
        return PlayerAddedMessage(game.id!!, message.player)
    }

    @MessageMapping("/games/addPlayer")
    @SendTo("/games")
    fun addGuess(
        @Payload message: AddGuessMessage,
    ): GuessAddedMessage {
        val game = games[message.gameId] ?: throw IllegalArgumentException("Game not found")
        game.addGuess(message.player, message.guess)
        return GuessAddedMessage(game.id!!, message.player)
    }
}
