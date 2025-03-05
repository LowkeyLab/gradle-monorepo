package io.github.tacascer.kotlinx.serialization.ber.dsl

// Explicitly import the extension functions to avoid unresolved references
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerSequenceTypesTest :
    FunSpec({
        test("encode simple SEQUENCE") {
            val sequence =
                Ber
                    .sequence {
                        +Ber.bool(true)
                        +Ber.int(42)
                    }.encode()

            // Verify structure
            sequence[0] shouldBe 0x30.toByte() // SEQUENCE tag

            // Fix: Correct size calculation for a SEQUENCE containing BOOLEAN and INTEGER
            // 1 byte SEQUENCE tag + 1 byte length + 6 bytes content (3 for BOOL + 3 for INT)
            sequence.size shouldBe 8

            // Additional verification of encoded structure
            // Format should be: 0x30 0x06 [BOOL encoding] [INT encoding]
            // BOOL encoding: 0x01 0x01 0xFF
            // INT encoding: 0x02 0x01 0x2A
            sequence[0] shouldBe 0x30.toByte() // SEQUENCE tag
            sequence[1] shouldBe 0x06.toByte() // Length of content (6)
            sequence[2] shouldBe 0x01.toByte() // BOOLEAN tag
            sequence[3] shouldBe 0x01.toByte() // BOOLEAN length (1)
            sequence[4] shouldBe 0xFF.toByte() // BOOLEAN value (true)
            sequence[5] shouldBe 0x02.toByte() // INTEGER tag
            sequence[6] shouldBe 0x01.toByte() // INTEGER length (1)
            sequence[7] shouldBe 0x2A.toByte() // INTEGER value (42)
        }

        test("encode nested SEQUENCE structure") {
            val complex =
                Ber
                    .sequence {
                        +Ber.bool(true)
                        +Ber.int(123)
                        +Ber.sequence {
                            +Ber.utf8String("Inner")
                            +Ber.nullValue()
                        }
                    }.encode()

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
                Ber
                    .set {
                        +Ber.int(1)
                        +Ber.int(2)
                        +Ber.bool(true)
                    }.encode()

            // Verify structure
            set[0] shouldBe 0x31.toByte() // SET tag
        }

        test("encode SET OF with sorting") {
            // SET OF sorts elements by their encoded value
            val setOf =
                Ber
                    .setOf {
                        +Ber.printableString("B") // Should be sorted after "A"
                        +Ber.printableString("A") // Should be sorted before "B"
                    }.encode()

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
            val explicitTag = explicitlyTagged(1uL) { +Ber.utf8String("Inside Tag") }.encode()

            explicitTag[0] shouldBe
                0xA1.toByte() // [1] EXPLICIT (constructed bit + context-specific)

            // Should contain a UTF8String inside
            val content = explicitTag.sliceArray(2 until explicitTag.size)
            (content.any { it == 0x0C.toByte() }) shouldBe true // UTF8String tag
        }

        test("encode IMPLICIT vs EXPLICIT tagging") {
            // Create a string with implicit tagging (should be primitive)
            val implicitString =
                (
                    Ber.utf8String("Test") withImplicitTag
                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC)
                ).encode()

            // Create a sequence with implicit tagging (should be constructed)
            val implicitSeq =
                (
                    Ber.sequence { +Ber.utf8String("Test") } withImplicitTag
                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC)
                ).encode()

            // Create an explicit tag
            val explicit =
                (
                    Ber.utf8String("Test") withExplicitTag
                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC)
                ).encode()

            // Implicit tag replaces the original tag
            implicitString[0] shouldBe 0x80.toByte() // [0] tag (primitive)
            implicitSeq[0] shouldBe 0xA0.toByte() // [0] tag (constructed)

            // Explicit tag wraps the original element
            explicit[0] shouldBe 0xA0.toByte() // [0] EXPLICIT with constructed bit
            explicit[2] shouldBe 0x0C.toByte() // Original UTF8String tag inside

            // Implicit encoding should be shorter since it doesn't include the original tag
            implicitString.size shouldBe
                (explicit.size - 2) // 2 bytes difference (tag + length)
        }

        test("complex certificate-like structure") {
            val certificate =
                Ber
                    .sequence {
                        +Ber.sequence {
                            // TBSCertificate
                            +Ber.int(2) // version
                            +Ber.int(12345) // serialNumber
                            +Ber.sequence {
                                // signature algorithm
                                +Ber.objectIdentifier(
                                    "1.2.840.113549.1.1.11",
                                ) // sha256WithRSAEncryption
                                +Ber.nullValue()
                            }
                            +Ber.sequence {
                                // issuer
                                +Ber.set {
                                    +Ber.sequence {
                                        +Ber.objectIdentifier("2.5.4.3") // commonName
                                        +Ber.printableString("Test CA")
                                    }
                                }
                            }
                            +Ber.sequence {
                                // validity
                                +Ber.utcTime(
                                    kotlinx.datetime.Clock.System
                                        .now(),
                                )
                                +Ber.utcTime(
                                    kotlinx.datetime.Clock.System
                                        .now(),
                                )
                            }
                            +Ber.sequence {
                                // subject
                                +Ber.set {
                                    +Ber.sequence {
                                        +Ber.objectIdentifier("2.5.4.3") // commonName
                                        +Ber.printableString("Test Subject")
                                    }
                                }
                            }
                        }
                        +Ber.sequence {
                            // signatureAlgorithm
                            +Ber.objectIdentifier(
                                "1.2.840.113549.1.1.11",
                            ) // sha256WithRSAEncryption
                            +Ber.nullValue()
                        }
                        +Ber.octetString(
                            byteArrayOf(1, 2, 3, 4, 5),
                        ) // signature value (dummy)
                    }.encode()

            // Just verify it encodes properly
            certificate[0] shouldBe 0x30.toByte() // SEQUENCE tag
            certificate.size shouldBeGreaterThan 20 // Should be reasonably large
        }
    })
