package com.github.lowkeylab.guesstheword.game

import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("games")
@MessageMapping("/game")
class GameController(
    private val gameService: GameService,
) {
    @GetMapping("{id}")
    fun getGame(
        @PathVariable id: String,
    ): Game? = gameService.get(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    private val games: MutableMap<String, Game> = mutableMapOf()

    @MessageMapping("/game/new")
    fun newGame(
        @Payload initialPlayer: Player,
    ): String {
        val game = gameService.new()
        game.addPlayer(initialPlayer)
        games[game.id!!] = game
        return game.id
    }

    @MessageMapping("/game/{id}/addPlayer")
    fun addPlayer(
        @DestinationVariable gameId: String,
        @Payload player: Player,
    ): Player {
        val game = games[gameId] ?: throw IllegalArgumentException("Game not found")
        game.addPlayer(player)
        return player
    }

    @MessageMapping("/game/{id}/start")
    fun startGame(
        @DestinationVariable gameId: String,
    ): Boolean {
        val game = games[gameId] ?: throw IllegalArgumentException("Game not found")
        game.start()
        return true
    }

    @MessageMapping("/game/{id}/addGuess")
    fun addGuess(
        @DestinationVariable gameId: String,
        @Payload message: AddGuessMessage,
    ): GuessAddedMessage {
        val game = games[gameId] ?: throw IllegalArgumentException("Game not found")
        game.addGuess(message.player, message.guess)
        return GuessAddedMessage(message.player, message.guess)
    }
}
