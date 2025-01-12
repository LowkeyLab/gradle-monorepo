package com.github.lowkeylab.guesstheword.game

class GameService(
    private val gameRepository: GameRepository,
) {
    fun new(): Game = gameRepository.save(Game())
}
