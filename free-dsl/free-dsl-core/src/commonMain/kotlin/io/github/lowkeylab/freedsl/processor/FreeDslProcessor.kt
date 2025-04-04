package io.github.lowkeylab.freedsl.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import io.github.lowkeylab.freedsl.FreeDsl

/**
 * Symbol processor for the [@FreeDsl][FreeDsl] annotation.
 *
 * This processor finds classes with primary constructors annotated with [@FreeDsl][FreeDsl] and generates
 * idiomatic Kotlin builders that support DSL syntax.
 */
class FreeDslProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("FreeDslProcessor: Processing started")

        // Find all classes annotated with @FreeDsl
        val symbols =
            resolver
                .getSymbolsWithAnnotation(FreeDsl::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.validate() }
                .toList()

        if (symbols.isEmpty()) {
            logger.info("FreeDslProcessor: No classes found with @FreeDsl annotation")
            return emptyList()
        }

        logger.info("FreeDslProcessor: Found ${symbols.size} classes with @FreeDsl annotation")

        // Process each annotated class
        symbols.forEach { classDeclaration ->
            try {
                processClass(classDeclaration)
            } catch (e: Exception) {
                logger.error("FreeDslProcessor: Error processing class ${classDeclaration.simpleName.asString()}: ${e.message}")
            }
        }

        // Return symbols that couldn't be processed in this round
        return symbols.filterNot { it.validate() }
    }

    private fun processClass(classDeclaration: KSClassDeclaration) {
        val className = classDeclaration.simpleName.asString()
        logger.info("FreeDslProcessor: Processing class $className")

        // Check if it has a primary constructor
        val primaryConstructor = classDeclaration.primaryConstructor
        if (primaryConstructor == null) {
            logger.error(
                "FreeDslProcessor: $className does not have a primary constructor. @FreeDsl can only be applied to classes with a primary constructor.",
            )
            return
        }

        // Check if the primary constructor has parameters
        if (primaryConstructor.parameters.isEmpty()) {
            logger.error(
                "FreeDslProcessor: $className's primary constructor has no parameters. @FreeDsl can only be applied to classes with parameters in the primary constructor.",
            )
            return
        }

        // Generate DSL builder code
        val dslBuilder = DslBuilder(classDeclaration, codeGenerator, logger)
        dslBuilder.generate()
    }
}
