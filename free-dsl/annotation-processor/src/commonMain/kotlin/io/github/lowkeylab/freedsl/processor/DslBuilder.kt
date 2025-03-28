package io.github.lowkeylab.freedsl.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability

/**
 * Generates DSL builder code for data classes annotated with @FreeDsl.
 */
class DslBuilder(
    private val classDeclaration: KSClassDeclaration,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    private val className = classDeclaration.simpleName.asString()
    private val packageName = classDeclaration.packageName.asString()
    private val builderClassName = "${className}Builder"
    private val constructorParameters = classDeclaration.primaryConstructor?.parameters ?: emptyList()

    /**
     * Generates the DSL builder code for the class.
     */
    fun generate() {
        logger.info("DslBuilder: Generating DSL builder for $className")

        val fileContent = buildString {
            appendLine("// Generated by FreeDsl KSP processor")
            appendLine("package $packageName")
            appendLine()
            appendLine("import kotlin.properties.Delegates")
            appendLine()

            // Generate the builder class
            generateBuilderClass(this)

            // Generate the DSL function
            generateDslFunction(this)
        }

        // Write the generated code to a file
        val fileName = "${className}DslBuilder.kt"
        val dependencies = Dependencies(true, classDeclaration.containingFile!!)

        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = fileName
        ).use { outputStream ->
            outputStream.writer().use { writer ->
                writer.write(fileContent)
            }
        }

        logger.info("DslBuilder: Successfully generated DSL builder for $className")
    }

    /**
     * Generates the builder class code.
     */
    private fun generateBuilderClass(builder: StringBuilder) {
        builder.appendLine("/**")
        builder.appendLine(" * Builder for [$className] that supports DSL syntax.")
        builder.appendLine(" */")
        builder.appendLine("class $builderClassName {")

        // Generate properties for each constructor parameter
        constructorParameters.forEach { parameter ->
            generateProperty(builder, parameter)
        }

        // Generate nested builders for complex properties
        constructorParameters.forEach { parameter ->
            if (isNestedBuilderCandidate(parameter)) {
                generateNestedBuilderMethod(builder, parameter)
            }
        }

        // Generate build method
        generateBuildMethod(builder)

        builder.appendLine("}")
        builder.appendLine()
    }

    /**
     * Generates a property for a constructor parameter.
     */
    private fun generateProperty(builder: StringBuilder, parameter: KSValueParameter) {
        val name = parameter.name?.asString() ?: return
        val type = parameter.type.resolve()
        val typeString = getTypeString(type)
        val isNullable = type.nullability == Nullability.NULLABLE
        val hasDefaultValue = parameter.hasDefault

        builder.append("    ")

        if (isNullable || hasDefaultValue) {
            // For nullable or parameters with default values, we can use simple var
            builder.appendLine("var $name: $typeString${if (isNullable) "?" else ""} = ${getDefaultValueForType(type)}")
        } else {
            // For required non-nullable parameters, use Delegates.notNull()
            builder.appendLine("var $name: $typeString by Delegates.notNull()")
        }
    }

    /**
     * Gets a string representation of a type, including generic type arguments.
     */
    private fun getTypeString(type: KSType): String {
        val typeName = type.declaration.qualifiedName?.asString() ?: "Any"

        // If the type has no arguments, just return the type name
        if (type.arguments.isEmpty()) {
            return typeName
        }

        // For generic types, include the type arguments
        val typeArgs = type.arguments.joinToString(", ") { arg ->
            val argType = arg.type?.resolve()
            if (argType != null) {
                getTypeString(argType) + if (argType.nullability == Nullability.NULLABLE) "?" else ""
            } else {
                "Any"
            }
        }

        return "$typeName<$typeArgs>"
    }

    /**
     * Generates a nested builder method for complex properties.
     */
    private fun generateNestedBuilderMethod(builder: StringBuilder, parameter: KSValueParameter) {
        val name = parameter.name?.asString() ?: return
        val type = parameter.type.resolve()
        val typeName = type.declaration.qualifiedName?.asString() ?: return
        val isNullable = type.nullability == Nullability.NULLABLE

        builder.appendLine()
        builder.appendLine("    /**")
        builder.appendLine("     * Configure the [$name] property using DSL syntax.")
        builder.appendLine("     */")

        if (isNullable) {
            builder.appendLine("    fun $name(block: ${typeName.removeSuffix("?")}Builder.() -> Unit) {")
            builder.appendLine("        val builder = ${typeName.removeSuffix("?")}Builder()")
            builder.appendLine("        builder.block()")
            builder.appendLine("        this.$name = builder.build()")
            builder.appendLine("    }")
        } else {
            builder.appendLine("    fun $name(block: ${typeName}Builder.() -> Unit) {")
            builder.appendLine("        val builder = ${typeName}Builder()")
            builder.appendLine("        builder.block()")
            builder.appendLine("        this.$name = builder.build()")
            builder.appendLine("    }")
        }
    }

    /**
     * Generates the build method that creates an instance of the class.
     */
    private fun generateBuildMethod(builder: StringBuilder) {
        builder.appendLine()
        builder.appendLine("    /**")
        builder.appendLine("     * Builds an instance of [$className] with the configured properties.")
        builder.appendLine("     */")
        builder.appendLine("    fun build(): $className {")
        builder.append("        return $className(")

        // Add constructor parameters
        constructorParameters.forEachIndexed { index, parameter ->
            val name = parameter.name?.asString() ?: return@forEachIndexed
            builder.append("$name = $name")
            if (index < constructorParameters.size - 1) {
                builder.append(", ")
            }
        }

        builder.appendLine(")")
        builder.appendLine("    }")
    }

    /**
     * Generates the DSL function that creates and configures a builder.
     */
    private fun generateDslFunction(builder: StringBuilder) {
        val functionName = className.decapitalize()

        builder.appendLine("/**")
        builder.appendLine(" * Creates a [$className] using DSL syntax.")
        builder.appendLine(" */")
        builder.appendLine("fun $functionName(block: $builderClassName.() -> Unit): $className {")
        builder.appendLine("    val builder = $builderClassName()")
        builder.appendLine("    builder.block()")
        builder.appendLine("    return builder.build()")
        builder.appendLine("}")
    }

    /**
     * Determines if a parameter should have a nested builder.
     */
    private fun isNestedBuilderCandidate(parameter: KSValueParameter): Boolean {
        val type = parameter.type.resolve()
        val declaration = type.declaration

        // Check if it's a data class (potential nested builder)
        if (declaration is KSClassDeclaration) {
            return declaration.modifiers.any { it.toString() == "data" }
        }

        return false
    }

    /**
     * Gets the default value for a type.
     */
    private fun getDefaultValueForType(type: KSType): String {
        return when (type.nullability) {
            Nullability.NULLABLE -> "null"
            else -> {
                val typeName = type.declaration.qualifiedName?.asString() ?: "Any"
                val typeString = getTypeString(type)

                when {
                    typeName == "kotlin.String" -> "\"\""
                    typeName == "kotlin.Int" -> "0"
                    typeName == "kotlin.Long" -> "0L"
                    typeName == "kotlin.Double" -> "0.0"
                    typeName == "kotlin.Float" -> "0.0f"
                    typeName == "kotlin.Boolean" -> "false"
                    typeName == "kotlin.collections.List" -> "emptyList<${getTypeArgString(type)}>()"
                    typeName == "kotlin.Array" -> "emptyArray<${getTypeArgString(type)}>()"
                    typeName == "kotlin.collections.Map" -> "emptyMap<${getMapTypeArgString(type)}>()"
                    typeName == "kotlin.collections.Set" -> "emptySet<${getTypeArgString(type)}>()"
                    else -> "null as $typeString" // This is a fallback and might not compile
                }
            }
        }
    }

    /**
     * Gets a string representation of a type's first type argument.
     */
    private fun getTypeArgString(type: KSType): String {
        if (type.arguments.isEmpty()) {
            return "Any"
        }

        val arg = type.arguments.first()
        val argType = arg.type?.resolve()
        return if (argType != null) {
            getTypeString(argType) + if (argType.nullability == Nullability.NULLABLE) "?" else ""
        } else {
            "Any"
        }
    }

    /**
     * Gets a string representation of a Map's key and value type arguments.
     */
    private fun getMapTypeArgString(type: KSType): String {
        if (type.arguments.size < 2) {
            return "Any, Any"
        }

        val keyArg = type.arguments[0]
        val valueArg = type.arguments[1]

        val keyType = keyArg.type?.resolve()
        val valueType = valueArg.type?.resolve()

        val keyString = if (keyType != null) {
            getTypeString(keyType) + if (keyType.nullability == Nullability.NULLABLE) "?" else ""
        } else {
            "Any"
        }

        val valueString = if (valueType != null) {
            getTypeString(valueType) + if (valueType.nullability == Nullability.NULLABLE) "?" else ""
        } else {
            "Any"
        }

        return "$keyString, $valueString"
    }

    /**
     * Decapitalizes the first character of a string.
     */
    private fun String.decapitalize(): String {
        if (isEmpty() || !first().isUpperCase()) return this
        return first().lowercase() + substring(1)
    }
}
