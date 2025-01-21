package com.github.lowkeylab.guesstheword.game

import org.springframework.transaction.annotation.Transactional

@Transactional
class GameService(
    private val gameRepository: GameRepository,
) {
    fun get(id: String): Game? = gameRepository.findById(id).orElse(null)

    fun new(): Game = gameRepository.save(Game())

    fun save(game: Game): Game = gameRepository.save(game)
}
