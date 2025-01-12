package com.github.lowkeylab.guesstheword.game

import com.github.lowkeylab.guesstheword.player.Player
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Transient

@Entity
class Game(
    @Transient
    val players: MutableList<Player> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    val id: Long? = null,
)
