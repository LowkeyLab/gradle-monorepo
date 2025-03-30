package io.github.lowkeylab.freedsl.test

import io.github.lowkeylab.freedsl.FreeDsl

/**
 * Test data class for the FreeDsl annotation.
 */
@FreeDsl
data class Person(
    val name: String,
    val age: Int,
    val email: String? = null,
    val address: Address? = null,
    val tags: List<String> = emptyList(),
)

/**
 * Nested test data class for the FreeDsl annotation.
 */
@FreeDsl
data class Address(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String = "USA",
)

/**
 * Test data class with complex properties.
 */
@FreeDsl
data class Project(
    val name: String,
    val description: String? = null,
    val owner: Person,
    val contributors: List<Person> = emptyList(),
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap(),
)
