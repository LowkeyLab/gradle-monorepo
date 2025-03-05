package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerSequenceTypesTest :
        FunSpec({
            test("encode simple SEQUENCE") {
                val sequence =
                        Ber.Sequence {
                                    +Ber.Bool(true)
                                    +Ber.Int(42)
                                }
                                .encode()

                // Verify structure
                sequence[0] shouldBe 0x30.toByte() // SEQUENCE tag
                sequence.size shouldBe
                        6 // 1 byte tag + 1 byte length + 4 bytes content (for BOOL + INT)
            }

            test("encode nested SEQUENCE structure") {
                val complex =
                        Ber.Sequence {
                                    +Ber.Bool(true)
                                    +Ber.Int(123)
                                    +Ber.Sequence {
                                        +Ber.Utf8String("Inner")
                                        +Ber.Null()
                                    }
                                }
                                .encode()

                // Verify structure (without checking exact bytes)
                // SEQUENCE containing BOOL(true), INT(123), and a nested SEQUENCE
                complex[0] shouldBe 0x30.toByte() // SEQUENCE tag
                complex[2] shouldBe 0x01.toByte() // BOOL tag
                complex[5] shouldBe 0x02.toByte() // INT tag

                // There should be a nested SEQUENCE somewhere after
                (complex.sliceArray(8 until complex.size).any { it == 0x30.toByte() }) shouldBe true
            }

            test("encode SET type") {
                val set =
                        Ber.Set {
                                    +Ber.Int(1)
                                    +Ber.Int(2)
                                    +Ber.Bool(true)
                                }
                                .encode()

                // Verify structure
                set[0] shouldBe 0x31.toByte() // SET tag
            }

            test("encode SET OF with sorting") {
                // SET OF sorts elements by their encoded value
                val setOf =
                        Ber.SetOf {
                                    +Ber.PrintableString("B") // Should be sorted after "A"
                                    +Ber.PrintableString("A") // Should be sorted before "B"
                                }
                                .encode()

                // Find the printable string tag positions
                val content = setOf.sliceArray(2 until setOf.size)
                val firstStringPosition =
                        content.indexOfFirst { it == 0x13.toByte() } // PRINTABLE STRING tag
                val secondStringPosition =
                        content.indexOfLast { it == 0x13.toByte() } // PRINTABLE STRING tag

                // The next byte after each tag would be the length (1), and then the character
                val firstChar = content[firstStringPosition + 2]
                val secondChar = content[secondStringPosition + 2]

                // Verify sorting happened - "A" should be first, then "B"
                firstChar shouldBe 'A'.code.toByte()
                secondChar shouldBe 'B'.code.toByte()
            }

            test("encode tagged SEQUENCE") {
                // [1] EXPLICIT SEQUENCE { UTF8String }
                val explicitTag = ExplicitlyTagged(1uL) { +Ber.Utf8String("Inside Tag") }.encode()

                explicitTag[0] shouldBe
                        0xA1.toByte() // [1] EXPLICIT (constructed bit + context-specific)

                // Should contain a UTF8String inside
                val content = explicitTag.sliceArray(2 until explicitTag.size)
                (content.any { it == 0x0C.toByte() }) shouldBe true // UTF8String tag
            }

            test("encode IMPLICIT vs EXPLICIT tagging") {
                // Create a SEQUENCE with one element, using both tagging types
                val implicit =
                        (Ber.Utf8String("Test") withImplicitTag
                                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC))
                                .encode()

                val explicit =
                        (Ber.Utf8String("Test") withExplicitTag
                                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC))
                                .encode()

                // Implicit tag replaces the original tag
                implicit[0] shouldBe 0x80.toByte() // [0] tag

                // Explicit tag wraps the original element
                explicit[0] shouldBe 0xA0.toByte() // [0] EXPLICIT with constructed bit
                explicit[2] shouldBe 0x0C.toByte() // Original UTF8String tag inside

                // Implicit encoding should be shorter since it doesn't include the original tag
                implicit.size shouldBe (explicit.size - 2) // 2 bytes difference (tag + length)
            }

            test("complex certificate-like structure") {
                val certificate =
                        Ber.Sequence {
                                    +Ber.Sequence { // TBSCertificate
                                        +Ber.Int(2) // version
                                        +Ber.Int(12345) // serialNumber
                                        +Ber.Sequence { // signature algorithm
                                            +Ber.ObjectIdentifier(
                                                    "1.2.840.113549.1.1.11"
                                            ) // sha256WithRSAEncryption
                                            +Ber.Null()
                                        }
                                        +Ber.Sequence { // issuer
                                            +Ber.Set {
                                                +Ber.Sequence {
                                                    +Ber.ObjectIdentifier("2.5.4.3") // commonName
                                                    +Ber.PrintableString("Test CA")
                                                }
                                            }
                                        }
                                        +Ber.Sequence { // validity
                                            +Ber.UtcTime(kotlinx.datetime.Clock.System.now())
                                            +Ber.UtcTime(kotlinx.datetime.Clock.System.now())
                                        }
                                        +Ber.Sequence { // subject
                                            +Ber.Set {
                                                +Ber.Sequence {
                                                    +Ber.ObjectIdentifier("2.5.4.3") // commonName
                                                    +Ber.PrintableString("Test Subject")
                                                }
                                            }
                                        }
                                    }
                                    +Ber.Sequence { // signatureAlgorithm
                                        +Ber.ObjectIdentifier(
                                                "1.2.840.113549.1.1.11"
                                        ) // sha256WithRSAEncryption
                                        +Ber.Null()
                                    }
                                    +Ber.OctetString(
                                            byteArrayOf(1, 2, 3, 4, 5)
                                    ) // signature value (dummy)
                                }
                                .encode()

                // Just verify it encodes properly
                certificate[0] shouldBe 0x30.toByte() // SEQUENCE tag
                certificate.size shouldBeGreaterThan 20 // Should be reasonably large
            }
        })
