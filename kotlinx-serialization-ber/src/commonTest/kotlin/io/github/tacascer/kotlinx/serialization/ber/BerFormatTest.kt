package io.github.tacascer.kotlinx.serialization.ber

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable

class BerFormatTest :
        FunSpec({
            val berFormat = BerFormat()

            test("serialize and deserialize primitive types") {
                @Serializable
                data class PrimitiveTypes(
                        val boolean: Boolean,
                        val byte: Byte,
                        val short: Short,
                        val int: Int,
                        val long: Long,
                        val float: Float,
                        val double: Double,
                        val char: Char,
                        val string: String
                )

                val original =
                        PrimitiveTypes(
                                boolean = true,
                                byte = 42,
                                short = 1000,
                                int = 100000,
                                long = 1000000000L,
                                float = 3.14f,
                                double = 2.71828,
                                char = 'X',
                                string = "Hello BER!"
                        )

                val bytes = berFormat.encodeToByteArray(PrimitiveTypes.serializer(), original)
                val deserialized = berFormat.decodeFromByteArray(PrimitiveTypes.serializer(), bytes)

                deserialized shouldBe original
            }
        })
