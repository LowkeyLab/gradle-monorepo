package io.github.lowkeylab.freedsl

/**
 * Demonstration of how the generated DSL builders would be used.
 *
 * Note: This is not an actual test that can be run immediately. It demonstrates
 * the expected usage of the DSL builders that will be generated by the KSP processor
 * when it processes the @FreeDsl annotation on the Person and Address classes.
 *
 * The KSP processor runs during the build process, so the DSL builders will be
 * generated when the project is built.
 *
 * Example of generated code usage:
 *
 * ```kotlin
 * // Create a Person using the generated DSL
 * val person = person {
 *     name = "John Doe"
 *     age = 30
 *     email = "john.doe@example.com"
 *     
 *     // Nested DSL for Address
 *     address {
 *         street = "123 Main St"
 *         city = "New York"
 *         zipCode = "10001"
 *         country = "USA"
 *     }
 * }
 * ```
 */
class FreeDslDemoUsage {
    // This class is just a placeholder to demonstrate how the generated code would be used.
    // The actual implementation will be generated by the KSP processor.
}
