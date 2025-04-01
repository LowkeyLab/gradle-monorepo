package io.github.lowkeylab.freedsl

/**
 * Annotation to mark data classes for which DSL builders should be generated.
 *
 * When applied to a data class, a KSP processor will generate an idiomatic Kotlin
 * builder that supports DSL syntax.
 *
 * Example usage:
 * ```kotlin
 * @FreeDsl
 * data class Person(
 *     val name: String,
 *     val age: Int,
 *     val address: Address? = null
 * )
 *
 * // Generated DSL usage will look like:
 * val person = person {
 *     name = "John"
 *     age = 30
 *     address {
 *         street = "Main St"
 *         city = "New York"
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class FreeDsl
