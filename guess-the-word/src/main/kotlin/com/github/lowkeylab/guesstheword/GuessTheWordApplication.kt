package com.github.lowkeylab.guesstheword

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GuessTheWordApplication

fun main(args: Array<String>) {
    runApplication<GuessTheWordApplication>(*args)
}
