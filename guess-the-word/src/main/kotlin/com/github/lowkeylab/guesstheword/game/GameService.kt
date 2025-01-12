package com.github.lowkeylab.guesstheword.game

import org.springframework.transaction.annotation.Transactional

@Transactional
class GameService(
    private val gameRepository: GameRepository,
) {
    fun get(id: String): Game? = gameRepository.findById(id).orElse(null)

    fun new(): Game = gameRepository.save(Game())

    fun addPlayer(
        game: Game,
        player: Player,
    ) {
        game.addPlayer(player)
        gameRepository.save(game)
    }

    fun start(game: Game) {
        game.start()
        gameRepository.save(game)
    }

    fun addGuess(
        game: Game,
        player: Player,
        guess: String,
    ) {
        game.addGuess(player, guess)
        gameRepository.save(game)
    }
}
