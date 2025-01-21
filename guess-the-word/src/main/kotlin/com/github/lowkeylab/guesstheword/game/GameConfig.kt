package com.github.lowkeylab.guesstheword.game

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@EnableMongoRepositories
@EnableWebSocketMessageBroker
@Configuration
class GameConfig : WebSocketMessageBrokerConfigurer {
    @Bean
    fun gameService(gameRepository: GameRepository): GameService = GameService(gameRepository)

    @Bean
    fun mongoTransactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager = MongoTransactionManager(dbFactory)

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
        registry.enableSimpleBroker("/topic")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/game")
    }
}
