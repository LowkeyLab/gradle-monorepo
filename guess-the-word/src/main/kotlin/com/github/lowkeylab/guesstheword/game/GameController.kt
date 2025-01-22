package com.github.lowkeylab.guesstheword.game

import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@RestController
@RequestMapping("games")
@MessageMapping("/game")
class GameController(
    private val gameService: GameService,
    val template: SimpMessagingTemplate,
) {
    @GetMapping("{id}")
    fun getGame(
        @PathVariable id: String,
    ): Game? = gameService.get(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    private val games: ConcurrentMap<String, Game> = ConcurrentHashMap()

    @MessageMapping("/new")
    fun newGame(): GameEvent {
        val game = gameService.new()
        games[game.id!!] = game
        return GameEvent(GameStartedMessage(game.id))
    }

    @MessageMapping("/{id}")
    fun processGameEvent(
        @DestinationVariable gameId: String,
        @Payload gameEvent: GameEvent,
    ): GameEvent {
        val game = games[gameId] ?: throw IllegalArgumentException("Game not found")
        return when (gameEvent.message) {
            is AddPlayerMessage -> {
                val player = gameEvent.message.player
                game.addPlayer(player)
                return GameEvent(PlayerAddedMessage(player))
            }
            is AddGuessMessage -> {
                game.addGuess(gameEvent.message.player, gameEvent.message.guess)
                return GameEvent(GuessAddedMessage(gameEvent.message.player, gameEvent.message.guess))
            }
            else -> TODO()
        }
    }
}
