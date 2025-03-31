package io.github.lowkeylab.freedsl.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlin.properties.Delegates

/**
 * Generates DSL builder code for data classes annotated with @FreeDsl.
 */
class DslBuilder(
    private val classDeclaration: KSClassDeclaration,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    companion object {
        // Kotlin primitive types
        private const val TYPE_INT = "kotlin.Int"
        private const val TYPE_LONG = "kotlin.Long"
        private const val TYPE_DOUBLE = "kotlin.Double"
        private const val TYPE_FLOAT = "kotlin.Float"
        private const val TYPE_BOOLEAN = "kotlin.Boolean"
        private const val TYPE_CHAR = "kotlin.Char"
        private const val TYPE_BYTE = "kotlin.Byte"
        private const val TYPE_SHORT = "kotlin.Short"

        // Kotlin non-primitive types
        private const val TYPE_STRING = "kotlin.String"
        private const val TYPE_ANY = "kotlin.Any"

        // Kotlin collection types
        private const val TYPE_LIST = "kotlin.collections.List"
        private const val TYPE_SET = "kotlin.collections.Set"
        private const val TYPE_MAP = "kotlin.collections.Map"
        private const val TYPE_ARRAY = "kotlin.Array"
    }

    private val className = classDeclaration.simpleName.asString()
    private val packageName = classDeclaration.packageName.asString()
    private val builderClassName = "${className}Builder"
    private val constructorParameters = classDeclaration.primaryConstructor?.parameters ?: emptyList()

    /**
     * Generates the DSL builder code for the class.
     */
    fun generate() {
        logger.info("DslBuilder: Generating DSL builder for $className")

        try {
            // Get the fully qualified class name
            val fullClassName =
                classDeclaration.qualifiedName?.asString()
                    ?: throw IllegalArgumentException("Class must have a qualified name")
            val classType = ClassName.bestGuess(fullClassName)
            val builderType = ClassName.bestGuess("$packageName.$builderClassName")

            // Create the builder class
            val builderClass =
                TypeSpec
                    .classBuilder(builderClassName)
                    .addKdoc("Builder for [$className] that supports DSL syntax.")

            // Add properties for each constructor parameter
            constructorParameters.forEach { parameter ->
                val propertySpec = generatePropertySpec(parameter)
                builderClass.addProperty(propertySpec)
            }

            // Add nested builders for complex properties
            constructorParameters.forEach { parameter ->
                if (isNestedBuilderCandidate(parameter)) {
                    val nestedBuilderMethod = generateNestedBuilderMethodSpec(parameter)
                    builderClass.addFunction(nestedBuilderMethod)
                }
            }

            // Add extension functions for list properties to support unary plus
            constructorParameters.forEach { parameter ->
                val type = parameter.type.resolve()
                val typeName = type.declaration.qualifiedName?.asString()
                if (typeName == TYPE_LIST) {
                    // Get the parameter name
                    val name = parameter.name?.asString() ?: return@forEach

                    // Get the type argument of the list
                    val typeArg =
                        type.arguments
                            .firstOrNull()
                            ?.type
                            ?.resolve() ?: return@forEach
                    val typeArgName = getTypeNameFromKSType(typeArg)

                    // Create a function to add an item to the list
                    // Try to singularize the name (simple heuristic)
                    val functionName =
                        if (name.endsWith("s")) {
                            name.substring(0, name.length - 1).decapitalize()
                        } else {
                            name.decapitalize() + "Item"
                        }

                    val addFunction =
                        FunSpec
                            .builder(functionName)
                            .addParameter("value", typeArgName)
                            .addStatement("%N.add(value)", name)
                            .build()

                    // Add the function to the builder class
                    builderClass.addFunction(addFunction)

                    // Add a block method for the list property
                    val blockMethod = generateListBlockMethodSpec(parameter)
                    builderClass.addFunction(blockMethod)
                }
            }

            // Add build method
            val buildMethod = generateBuildMethodSpec(classType)
            builderClass.addFunction(buildMethod)

            // Create the DSL function
            val dslFunction = generateDslFunctionSpec(classType, builderType)

            // Create the file spec
            val fileSpecBuilder =
                FileSpec
                    .builder(packageName, "${className}DslBuilder")
                    .addFileComment("Generated by FreeDsl KSP processor")

            val fileSpec =
                fileSpecBuilder
                    .addType(builderClass.build())
                    .addFunction(dslFunction)
                    .build()

            // Write the generated code to a file
            val dependencies = Dependencies(true, classDeclaration.containingFile!!)

            codeGenerator
                .createNewFile(
                    dependencies = dependencies,
                    packageName = packageName,
                    fileName = "${className}DslBuilder",
                ).use { outputStream ->
                    outputStream.writer().use { writer ->
                        fileSpec.writeTo(writer)
                    }
                }

            logger.info("DslBuilder: Successfully generated DSL builder for $className")
        } catch (e: Exception) {
            logger.error("DslBuilder: Error generating DSL builder for $className: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Determines if a type is a collection type.
     *
     * @param type The KSType to check
     * @return True if the type is a collection type, false otherwise
     */
    private fun isCollectionType(type: KSType): Boolean {
        val typeName = type.declaration.qualifiedName?.asString() ?: return false
        return typeName == TYPE_LIST || typeName == TYPE_SET || typeName == TYPE_MAP || typeName == TYPE_ARRAY
    }

    /**
     * Generates a PropertySpec for a constructor parameter.
     *
     * @param parameter The constructor parameter
     * @return The PropertySpec for the parameter
     */
    private fun generatePropertySpec(parameter: KSValueParameter): PropertySpec {
        val name = parameter.name?.asString() ?: throw IllegalArgumentException("Parameter must have a name")
        val type = parameter.type.resolve()
        val typeName = getTypeNameFromKSType(type)
        val isNullable = type.nullability == Nullability.NULLABLE
        val hasDefaultValue = parameter.hasDefault
        val isCollection = isCollectionType(type)
        val isList = type.declaration.qualifiedName?.asString() == TYPE_LIST

        val propertyBuilder =
            PropertySpec
                .builder(name, typeName)
                .mutable(true) // Make it a var

        when {
            isCollection && isList -> {
                // For List types, initialize with a mutable list
                val typeArg =
                    getTypeNameFromKSType(
                        type.arguments
                            .first()
                            .type!!
                            .resolve(),
                    )
                propertyBuilder.initializer("mutableListOf<%T>()", typeArg)
            }

            isNullable || hasDefaultValue -> {
                // For nullable or parameters with default values, we can use simple var with initializer
                val defaultValue = getDefaultValueCodeBlock(type)
                propertyBuilder.initializer(defaultValue)
            }

            !isPrimitiveType(type) -> {
                // For required non-nullable non-primitive types, use lateinit
                propertyBuilder.addModifiers(com.squareup.kotlinpoet.KModifier.LATEINIT)
            }

            else -> {
                // For required non-nullable primitive types, use Delegates.notNull()
                val delegateType = Delegates::class.asClassName()
                propertyBuilder.delegate("$delegateType.notNull()")
            }
        }

        return propertyBuilder.build()
    }

    /**
     * Determines if a type is a primitive type.
     *
     * @param type The KSType to check
     * @return True if the type is a primitive type, false otherwise
     */
    private fun isPrimitiveType(type: KSType): Boolean {
        val typeName = type.declaration.qualifiedName?.asString() ?: return false

        return when (typeName) {
            TYPE_INT, TYPE_LONG, TYPE_DOUBLE, TYPE_FLOAT,
            TYPE_BOOLEAN, TYPE_CHAR, TYPE_BYTE, TYPE_SHORT,
            -> true

            else -> false
        }
    }

    /**
     * Converts a KSType to a TypeName that can be used with KotlinPoet.
     *
     * @param type The KSType to convert
     * @return The corresponding TypeName
     */
    private fun getTypeNameFromKSType(type: KSType): TypeName {
        val declaration = type.declaration
        val typeName = declaration.qualifiedName?.asString() ?: return Any::class.asTypeName()
        val isNullable = type.nullability == Nullability.NULLABLE

        // Handle primitive types
        val primitiveTypeName =
            when (typeName) {
                TYPE_STRING -> String::class.asClassName()
                TYPE_INT -> Int::class.asClassName()
                TYPE_LONG -> Long::class.asClassName()
                TYPE_DOUBLE -> Double::class.asClassName()
                TYPE_FLOAT -> Float::class.asClassName()
                TYPE_BOOLEAN -> Boolean::class.asClassName()
                TYPE_ANY -> Any::class.asTypeName()
                TYPE_LIST -> ClassName.bestGuess("kotlin.collections.MutableList")
                else -> ClassName.bestGuess(typeName)
            }

        // If the type has no arguments, just return the type name
        if (type.arguments.isEmpty()) {
            return if (isNullable) primitiveTypeName.copy(nullable = true) else primitiveTypeName
        }

        // For generic types, include the type arguments
        val typeArgs =
            type.arguments.map { arg ->
                val argType = arg.type?.resolve()
                if (argType != null) {
                    getTypeNameFromKSType(argType)
                } else {
                    Any::class.asTypeName()
                }
            }

        val parameterizedTypeName = primitiveTypeName.parameterizedBy(typeArgs)
        return if (isNullable) parameterizedTypeName.copy(nullable = true) else parameterizedTypeName
    }

    /**
     * Gets a CodeBlock with the default value for a type.
     *
     * @param type The KSType to get the default value for
     * @return A CodeBlock with the default value
     */
    private fun getDefaultValueCodeBlock(type: KSType): CodeBlock =
        when (type.nullability) {
            Nullability.NULLABLE -> CodeBlock.of("null")
            else -> {
                val typeName = type.declaration.qualifiedName?.asString() ?: TYPE_ANY

                when (typeName) {
                    TYPE_STRING -> CodeBlock.of("%S", "")
                    TYPE_INT -> CodeBlock.of("%L", 0)
                    TYPE_LONG -> CodeBlock.of("%LL", 0)
                    TYPE_DOUBLE -> CodeBlock.of("%L", 0.0)
                    TYPE_FLOAT -> CodeBlock.of("%Lf", 0.0)
                    TYPE_BOOLEAN -> CodeBlock.of("%L", false)
                    TYPE_LIST -> {
                        val typeArg =
                            getTypeNameFromKSType(
                                type.arguments
                                    .first()
                                    .type!!
                                    .resolve(),
                            )
                        CodeBlock.of("mutableListOf<%T>()", typeArg)
                    }

                    TYPE_ARRAY -> {
                        val typeArg =
                            getTypeNameFromKSType(
                                type.arguments
                                    .first()
                                    .type!!
                                    .resolve(),
                            )
                        CodeBlock.of("emptyArray<%T>()", typeArg)
                    }

                    TYPE_MAP -> {
                        val keyType = getTypeNameFromKSType(type.arguments[0].type!!.resolve())
                        val valueType = getTypeNameFromKSType(type.arguments[1].type!!.resolve())
                        CodeBlock.of("emptyMap<%T, %T>()", keyType, valueType)
                    }

                    TYPE_SET -> {
                        val typeArg =
                            getTypeNameFromKSType(
                                type.arguments
                                    .first()
                                    .type!!
                                    .resolve(),
                            )
                        CodeBlock.of("emptySet<%T>()", typeArg)
                    }

                    else ->
                        CodeBlock.of(
                            "null as %T",
                            getTypeNameFromKSType(type),
                        )
                    // This is a fallback and might not compile
                }
            }
        }

    /**
     * Generates a FunSpec for a nested builder method.
     *
     * @param parameter The parameter to generate a nested builder method for
     * @return The FunSpec for the nested builder method
     */
    private fun generateNestedBuilderMethodSpec(parameter: KSValueParameter): FunSpec {
        val name = parameter.name?.asString() ?: throw IllegalArgumentException("Parameter must have a name")
        val type = parameter.type.resolve()
        val typeName =
            type.declaration.qualifiedName?.asString() ?: throw IllegalArgumentException("Parameter must have a type")
        val isNullable = type.nullability == Nullability.NULLABLE

        // Create the builder class name
        val builderClassName =
            if (isNullable) {
                "${typeName.removeSuffix("?")}Builder"
            } else {
                "${typeName}Builder"
            }
        val builderClassType = ClassName.bestGuess(builderClassName)

        // Create the lambda type for the block parameter
        val lambdaType =
            LambdaTypeName.get(
                receiver = builderClassType,
                returnType = Unit::class.asClassName(),
            )

        // Create the function
        return FunSpec
            .builder(name)
            .addKdoc("Configure the [$name] property using DSL syntax.")
            .addParameter("block", lambdaType)
            .addStatement("val builder = %T()", builderClassType)
            .addStatement("builder.block()")
            .addStatement("this.%N = builder.build()", name)
            .build()
    }

    /**
     * Generates a FunSpec for the build method.
     *
     * @param classType The TypeName for the class
     * @return The FunSpec for the build method
     */
    private fun generateBuildMethodSpec(classType: TypeName = ClassName.bestGuess(className)): FunSpec {
        // Create the function
        val funBuilder =
            FunSpec
                .builder("build")
                .addKdoc("Builds an instance of [$className] with the configured properties.")
                .returns(classType)

        // Build the constructor call with named parameters
        val codeBlockBuilder = CodeBlock.builder().add("return %T(\n", classType)

        // Add constructor parameters
        constructorParameters.forEachIndexed { index, parameter ->
            val name = parameter.name?.asString() ?: return@forEachIndexed
            codeBlockBuilder.add("    %N = %N", name, name)
            if (index < constructorParameters.size - 1) {
                codeBlockBuilder.add(",\n")
            } else {
                codeBlockBuilder.add("\n")
            }
        }

        codeBlockBuilder.add(")")

        funBuilder.addCode(codeBlockBuilder.build())

        return funBuilder.build()
    }

    /**
     * Generates a FunSpec for the DSL function.
     *
     * @param classType The TypeName for the class
     * @param builderType The TypeName for the builder class
     * @return The FunSpec for the DSL function
     */
    private fun generateDslFunctionSpec(
        classType: TypeName = ClassName.bestGuess(className),
        builderType: TypeName = ClassName.bestGuess(builderClassName),
    ): FunSpec {
        val functionName = className.decapitalize()

        // Create the lambda type for the block parameter
        val lambdaType =
            LambdaTypeName.get(
                receiver = builderType,
                returnType = Unit::class.asClassName(),
            )

        // Create the function
        return FunSpec
            .builder(functionName)
            .addKdoc("Creates a [$className] using DSL syntax.")
            .addParameter("block", lambdaType)
            .returns(classType)
            .addStatement("val builder = %T()", builderType)
            .addStatement("builder.block()")
            .addStatement("return builder.build()")
            .build()
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
     * Decapitalizes the first character of a string.
     */
    private fun String.decapitalize(): String {
        if (isEmpty() || !first().isUpperCase()) return this
        return first().lowercase() + substring(1)
    }

    /**
     * Generates a FunSpec for a list block method.
     *
     * @param parameter The parameter to generate a list block method for
     * @return The FunSpec for the list block method
     */
    private fun generateListBlockMethodSpec(parameter: KSValueParameter): FunSpec {
        val name = parameter.name?.asString() ?: throw IllegalArgumentException("Parameter must have a name")
        val type = parameter.type.resolve()

        // Get the type argument of the list
        val typeArg =
            type.arguments
                .firstOrNull()
                ?.type
                ?.resolve() ?: throw IllegalArgumentException("List must have a type argument")
        val typeArgName = getTypeNameFromKSType(typeArg)

        // Create the lambda type for the block parameter with the mutable list as receiver
        val listType = ClassName.bestGuess("kotlin.collections.MutableList").parameterizedBy(typeArgName)
        val lambdaType =
            LambdaTypeName.get(
                receiver = listType,
                returnType = Unit::class.asClassName(),
            )

        // Create the function
        return FunSpec
            .builder(name)
            .addKdoc("Configure the [$name] property using block syntax.")
            .addParameter("block", lambdaType)
            .addStatement("$name.apply { block() }")
            .build()
    }
}
