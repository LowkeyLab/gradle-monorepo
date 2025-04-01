# Free-DSL: Kotlin DSL Builder Generator

## Overview

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.lowkeylab/free-dsl-core)

Free-DSL is a Kotlin Multiplatform library that generates idiomatic Kotlin DSL
builders for data classes and regular classes with primary constructors.
It uses Kotlin Symbol Processing (KSP) to generate extension functions and
builder classes that enable
a clean, type-safe DSL syntax for constructing instances of your classes.

## Features

- Simple annotation-based API
- Generates idiomatic Kotlin DSL builders
- Supports both data classes and regular classes with primary constructors
- Supports nested DSL structures
- Handles nullable properties and default values
- Works with Kotlin Multiplatform projects

## Usage

### 1. Add the dependency

```kotlin
dependencies {
    implementation("com.github.lowkeylab:free-dsl-core:latest.release")
    ksp("com.github.lowkeylab:free-dsl-core:latest.release")
}
```

### 2. Annotate your classes

```kotlin
import io.github.lowkeylab.freedsl.FreeDsl

// Annotate data classes
@FreeDsl
data class Person(
    val name: String,
    val age: Int,
    val email: String? = null,
    val address: Address? = null
)

@FreeDsl
data class Address(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String = "USA"
)

// Annotate regular classes with primary constructors
@FreeDsl
class RegularClass(
    val name: String,
    val value: Int,
    val description: String? = null,
)
```

### 3. Build your project

The KSP processor will generate DSL builder code for your annotated classes (
both data classes and regular classes with primary constructors).

### 4. Use the generated DSL

```kotlin
// Create a Person using the generated DSL for a data class
val person = person {
    name = "John Doe"
    age = 30
    email = "john.doe@example.com"

    // Nested DSL for Address
    address {
        street = "123 Main St"
        city = "New York"
        zipCode = "10001"
        country = "USA"
    }
}

// Create a RegularClass using the generated DSL for a regular class with a primary constructor
val regularClass = regularClass {
    name = "Test Regular Class"
    value = 42
    description = "This is a test regular class"
}
```

## How It Works

When you annotate a data class or a regular class with a primary constructor
with `@FreeDsl`, the KSP processor generates:

1. A builder class for your annotated class
2. Properties in the builder for each constructor parameter
3. Nested builder methods for complex properties
4. A top-level DSL function that creates and configures the builder

The generated code follows Kotlin conventions and best practices for DSL design.

## Project Structure

```text
free-dsl/
├── build.gradle.kts       # Module build configuration
└── src/
    ├── commonMain/        # Common Kotlin source code
    │   ├── kotlin/        # Kotlin source files
    │   └── resources/     # Resources (KSP service provider configuration)
    └── commonTest/        # Common test code
```

## Development Setup

### Prerequisites

- JDK 21
- Gradle (use the wrapper `./gradlew`)

### Running Tests

To run tests, use:

```bash
./gradlew :free-dsl:check
```

Tests should be written using Kotest, which is already configured in the
project.

## Code Style and Quality

### Code Style

This project uses ktlint for code formatting, applied through the Spotless
plugin. To format your code:

```bash
./gradlew :free-dsl:spotlessApply
```

To check if your code follows the style guidelines:

```bash
./gradlew :free-dsl:spotlessCheck
```

### Code Quality

- All warnings are treated as errors (configured in the
  kotlin-multiplatform-conventions plugin)
- Code coverage is tracked using Kover

## Development Workflow

### Adding New Features

1. Create a new branch from `main`
2. Implement your feature with appropriate tests
3. Ensure all tests pass: `./gradlew :free-dsl:test`
4. Format your code: `./gradlew :free-dsl:spotlessApply`
5. Check code coverage: `./gradlew :free-dsl:koverReport`
6. Create a pull request

### Continuous Integration

The project uses CI tasks defined in the monorepo-conventions plugin:

- `checkCI`: Runs checks across all modules
- `buildCI`: Builds all modules
- `lintCI`: Runs linters across all modules
- `releaseCI`: Handles release tasks

## Publishing

Currently, no specific publishing configuration is set up for this module. When
ready for publishing, consider following the pattern used in the
kotlinx-serialization-ber module, which uses the vanniktech-maven-publish
plugin.

## Dependencies

Dependencies should be managed through the version catalog in the root project.
Add new dependencies to the version catalog rather than specifying versions
directly in the build.gradle.kts file.

## Inspiration

This project is a spiritual successor
to [FreeBuilder](https://github.com/inferred/FreeBuilder)
