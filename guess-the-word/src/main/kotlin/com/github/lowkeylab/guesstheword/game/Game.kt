package com.github.lowkeylab.guesstheword.game

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Game(
    @Id val id: String? = null,
)
