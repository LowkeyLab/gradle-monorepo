package com.github.lowkeylab.guesstheword.game

import org.springframework.data.repository.CrudRepository

interface GameRepository : CrudRepository<Game, String>
