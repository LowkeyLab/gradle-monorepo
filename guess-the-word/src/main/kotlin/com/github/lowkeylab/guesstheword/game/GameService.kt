package com.github.lowkeylab.guesstheword.game

class GameService(
    private val repository: GameRepository,
) {
    fun new(): Game = repository.save(Game())
}
