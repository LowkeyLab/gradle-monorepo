package com.github.lowkeylab.guesstheword.player

import jakarta.persistence.Embeddable

@Embeddable
class Player(
    val name: String,
)
