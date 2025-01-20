package com.github.lowkeylab.guesstheword.game.websocket

import com.github.lowkeylab.guesstheword.game.GameService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class GameController(
    private val gameService: GameService,
) {
    @MessageMapping("/games/new")
    @SendTo("/games")
    fun newGame(message: CreateGameMessage): GameCreatedMessage {
        val game = gameService.new()
        game.addPlayer(message.player)
        return GameCreatedMessage(game.id!!)
    }
}
