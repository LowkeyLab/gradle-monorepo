package com.github.lowkeylab.guesstheword.game

data class AddGuessMessage(
    val player: Player,
    val guess: String,
)

data class GuessAddedMessage(
    val player: Player,
    val guess: String,
)
