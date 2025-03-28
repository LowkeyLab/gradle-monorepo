# Free-DSL Module Guidelines

## Overview
Free-DSL is a Kotlin Multiplatform library for creating flexible Domain-Specific Languages (DSLs) in Kotlin. This module is part of the gradle-monorepo project.

## Project Structure
```
free-dsl/
├── build.gradle.kts       # Module build configuration
└── src/
    └── commonMain/        # Common Kotlin source code
        └── kotlin/        # Kotlin source files
```

## Development Setup

### Prerequisites
- JDK 21
- Gradle (use the wrapper `./gradlew`)

### Building the Project
To build the project, run:
```bash
./gradlew :free-dsl:build
```

### Running Tests
To run tests, use:
```bash
./gradlew :free-dsl:test
```

Tests should be written using Kotest, which is already configured in the project.

## Code Style and Quality

### Code Style
This project uses ktlint for code formatting, applied through the Spotless plugin. To format your code:
```bash
./gradlew :free-dsl:spotlessApply
```

To check if your code follows the style guidelines:
```bash
./gradlew :free-dsl:spotlessCheck
```

### Code Quality
- All warnings are treated as errors (configured in the kotlin-multiplatform-conventions plugin)
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
Currently, no specific publishing configuration is set up for this module. When ready for publishing, consider following the pattern used in the kotlinx-serialization-ber module, which uses the vanniktech-maven-publish plugin.

## Dependencies
Dependencies should be managed through the version catalog in the root project. Add new dependencies to the version catalog rather than specifying versions directly in the build.gradle.kts file.