package io.github.lowkeylab.freedsl.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the FreeDsl symbol processor.
 *
 * This class is responsible for creating instances of [FreeDslProcessor] which processes
 * classes annotated with the [@FreeDsl][io.github.lowkeylab.freedsl.FreeDsl] annotation.
 */
class FreeDslProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        FreeDslProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
}
