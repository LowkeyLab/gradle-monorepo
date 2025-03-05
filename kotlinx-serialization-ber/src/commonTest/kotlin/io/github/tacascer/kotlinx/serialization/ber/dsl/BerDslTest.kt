package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock

class BerDslTest :
        FunSpec({
            test("should create simple BER structures") {
                val berStructure =
                        Ber.Sequence {
                            +Ber.Bool(true)
                            +Ber.Int(42)
                            +Ber.Utf8String("Hello BER!")
                        }

                val bytes = berStructure.encode()
                bytes.isNotEmpty() shouldBe true

                // Check first few bytes (SEQUENCE tag + length)
                bytes[0] shouldBe 0x30.toByte() // SEQUENCE tag
            }

            test("should create complex nested structures") {
                val berStructure =
                        Ber.Sequence {
                            +Ber.Set {
                                +Ber.Int(1)
                                +Ber.Int(2)
                                +Ber.Int(3)
                            }
                            +Ber.SetOf {
                                +Ber.PrintableString("Hello")
                                +Ber.PrintableString("World")
                            }
                            +Ber.Sequence {
                                +Ber.Bool(false)
                                +Ber.Null()
                                +Ber.ObjectIdentifier(
                                        "1.2.840.113549.1.1.11"
                                ) // sha256WithRSAEncryption
                            }
                        }

                val bytes = berStructure.encode()
                bytes.isNotEmpty() shouldBe true

                // Basic tag checks
                bytes[0] shouldBe 0x30.toByte() // SEQUENCE tag
                bytes[2] shouldBe 0x31.toByte() // SET tag
            }

            test("should handle tagging") {
                val berStructure =
                        Ber.Sequence {
                            // Implicit tagging
                            +(Ber.Utf8String("Hello") withImplicitTag
                                    (0x1uL withClass BerTagClass.CONTEXT_SPECIFIC))

                            // Explicit tagging
                            +(Ber.Sequence { +Ber.Int(123) } withExplicitTag
                                    (0x2uL withClass BerTagClass.CONTEXT_SPECIFIC))

                            // Using ExplicitlyTagged helper
                            +ExplicitlyTagged(0x3uL) { +Ber.Bool(true) }
                        }

                val bytes = berStructure.encode()
                bytes.isNotEmpty() shouldBe true

                // Check for context-specific tags
                bytes[0] shouldBe 0x30.toByte() // SEQUENCE tag
                bytes[2] shouldBe 0x80.toByte() // [0] IMPLICIT tag
                // Next tag should be [2] EXPLICIT at some offset
            }

            test("should handle time types") {
                val now = Clock.System.now()

                val berStructure =
                        Ber.Sequence {
                            +Ber.UtcTime(now)
                            +Ber.GeneralizedTime(now)
                        }

                val bytes = berStructure.encode()
                bytes.isNotEmpty() shouldBe true
            }

            test("should create complex example matching the inspiration") {
                val berStructure =
                        Ber.Sequence {
                            +ExplicitlyTagged(1uL) { +Ber.Bool(false) }
                            +Ber.Set {
                                +Ber.Sequence {
                                    +Ber.SetOf {
                                        +Ber.PrintableString("World")
                                        +Ber.PrintableString("Hello")
                                    }
                                    +Ber.Set {
                                        +Ber.PrintableString("World")
                                        +Ber.PrintableString("Hello")
                                        +Ber.Utf8String("!!!")
                                    }
                                }
                            }
                            +Ber.Null()

                            +Ber.ObjectIdentifier("1.2.603.624.97")

                            +(Ber.Utf8String("Foo") withImplicitTag
                                    (0xCAFEuL withClass BerTagClass.PRIVATE))
                            +Ber.PrintableString("Bar")

                            // Fake Primitive (SEQUENCE with implicit tag, removing CONSTRUCTED bit)
                            +(Ber.Sequence { +Ber.Int(42) } withImplicitTag
                                    (0x5EUL without CONSTRUCTED))

                            +Ber.Set {
                                +Ber.Int(3)
                                +Ber.Int(-65789876543L)
                                +Ber.Bool(false)
                                +Ber.Bool(true)
                            }
                            +Ber.Sequence {
                                +Ber.Null()
                                +Ber.NumericString("12345")
                                +Ber.UtcTime(Clock.System.now())
                            }
                        } withExplicitTag (1337uL withClass BerTagClass.APPLICATION)

                val bytes = berStructure.encode()
                bytes.isNotEmpty() shouldBe true
            }
        })
