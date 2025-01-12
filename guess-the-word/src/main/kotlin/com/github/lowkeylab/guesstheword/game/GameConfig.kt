package com.github.lowkeylab.guesstheword.game

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories
@Configuration
class GameConfig {
    @Bean
    fun gameService(gameRepository: GameRepository): GameService = GameService(gameRepository)
}
