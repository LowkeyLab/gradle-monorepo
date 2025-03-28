package io.github.lowkeylab.freedsl

/**
 * Test data class for the FreeDsl annotation.
 */
@FreeDsl
data class Person(
    val name: String,
    val age: Int,
    val email: String? = null,
    val address: Address? = null
)

/**
 * Nested test data class for the FreeDsl annotation.
 */
@FreeDsl
data class Address(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String = "USA"
)