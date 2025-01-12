package com.github.lowkeylab.guesstheword.game

class GameService(
    private val gameRepository: GameRepository,
) {
    fun new(): Game = gameRepository.save(Game())

    fun addPlayer(
        game: Game,
        player: Player,
    ) {
        game.addPlayer(player)
        gameRepository.save(game)
    }
}
