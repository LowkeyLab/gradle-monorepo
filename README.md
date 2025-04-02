# gradle-monorepo

A Gradle monorepo containing multiple projects, each with its own build and
configuration.

## Projects

### [free-dsl](free-dsl/README.md)

A Kotlin Multiplatform library that generates idiomatic Kotlin DSL builders for
data classes and regular classes with primary constructors.
It uses Kotlin Symbol Processing (KSP) to generate extension functions and
builder classes that enable a clean, type-safe DSL syntax.

### guess-the-word

A Spring Boot application that uses MongoDB for data storage, WebSockets for
real-time communication, and provides a web interface.
It includes OpenAPI documentation and is containerized with Docker.

### kotlinx-serialization-ber

A Kotlin Multiplatform library for encoding and decoding BER (Basic Encoding
Rules) data. It uses kotlinx-serialization and is published to Maven Central.

### monorepo-convention-plugins

A collection of Gradle convention plugins used by the monorepo.
It includes internal convention plugins and a settings convention plugin.

### predix

A Spring Boot application that uses JPA for data access, PostgreSQL as the
database, and Liquibase for database migrations.
It includes a web interface with OpenAPI documentation and uses MapStruct for
object mapping.

### pto-scheduler

A Spring Boot application for PTO (Paid Time Off) scheduling.
It uses JPA for data access, PostgreSQL as the database,
Liquibase for database migrations, and has both a web interface and WebFlux for
reactive programming.
It includes OpenAPI documentation and is containerized with Docker.

## Building the Monorepo

This monorepo uses Gradle's composite build feature, where each project is
included as a separate build. To build the entire monorepo:

```bash
./gradlew build
```

To build a specific project:

```bash
./gradlew :projectName:build
```

For example:

```bash
./gradlew :free-dsl:build
```

## Running Tests

To run tests for the entire monorepo:

```bash
./gradlew test
```

To run tests for a specific project:

```bash
./gradlew :projectName:test
```

## Other Common Tasks

- Run linters: `./gradlew lint`

## Contribution Guidelines

Run `./gradlew tasks` to see which tasks are available.
