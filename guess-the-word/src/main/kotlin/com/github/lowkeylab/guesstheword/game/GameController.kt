package com.github.lowkeylab.guesstheword.game

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("games")
class GameController(
    private val gameService: GameService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun newGame(): Game = gameService.new()

    @GetMapping("{id}")
    fun getGame(
        @PathVariable id: String,
    ): Game? = gameService.get(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
