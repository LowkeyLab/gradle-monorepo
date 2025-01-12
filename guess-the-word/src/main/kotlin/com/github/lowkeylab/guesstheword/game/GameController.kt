package com.github.lowkeylab.guesstheword.game

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("games")
class GameController(
    private val gameService: GameService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun newGame(): Game = gameService.new()
}
