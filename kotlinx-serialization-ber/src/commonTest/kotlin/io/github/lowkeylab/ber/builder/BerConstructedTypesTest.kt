package io.github.lowkeylab.ber.builder

import io.github.lowkeylab.ber.BerTagClass
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class BerConstructedTypesTest :
    FunSpec({
        test("encode simple SEQUENCE") {
            val sequence =
                Ber
                    .sequence {
                        +Ber.bool(true)
                        +Ber.int(42)
                    }.encode()

            sequence.toHexString() shouldBe "30060101FF02012A"
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

            complex.toHexString() shouldContain "30" // SEQUENCE tag
            complex.toHexString() shouldContain "0101FF" // BOOLEAN true
            complex.toHexString() shouldContain "02017B" // INTEGER 123
            complex.toHexString() shouldContain "0C05496E6E6572" // UTF8String "Inner"
            complex.toHexString() shouldContain "0500" // NULL
        }

        test("encode SET type") {
            val set =
                Ber
                    .set {
                        +Ber.int(1)
                        +Ber.int(2)
                        +Ber.bool(true)
                    }.encode()

            set.toHexString().startsWith("31") shouldBe true // SET tag
            set.toHexString() shouldContain "0101FF" // BOOLEAN true
            set.toHexString() shouldContain "020101" // INTEGER 1
            set.toHexString() shouldContain "020102" // INTEGER 2
        }

        test("encode SET OF with sorting") {
            val setOf =
                Ber
                    .setOf {
                        +Ber.int(1)
                        +Ber.int(2)
                    }.encode()

            setOf.toHexString() shouldBe "3106020101020102"
        }

        test("encode tagged SEQUENCE") {
            val explicitTag =
                explicitlyTagged(1uL) {
                    +Ber.utf8String("Inside Tag")
                }.encode()

            explicitTag.toHexString().startsWith("A1") shouldBe true // [1] EXPLICIT tag
            explicitTag.toHexString() shouldContain "496E7369646520546167" // UTF8String
        }

        test("encode IMPLICIT vs EXPLICIT tagging") {
            val implicitString =
                (
                    Ber.utf8String("Test") withImplicitTag
                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC)
                ).encode()

            val implicitSeq =
                (
                    Ber.sequence { +Ber.utf8String("Test") } withImplicitTag
                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC)
                ).encode()

            val explicit =
                (
                    Ber.utf8String("Test") withExplicitTag
                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC)
                ).encode()

            implicitString.toHexString().startsWith("80") shouldBe true // [0] tag (primitive)
            implicitSeq.toHexString().startsWith("A0") shouldBe true // [0] tag (constructed)
            explicit.toHexString() shouldContain "0C0454657374" // Original UTF8String inside
        }

        test("encode SEQUENCE with unbounded length") {
            val sequence =
                Ber
                    .sequence {
                        unbounded() // Enable unbounded length encoding
                        +Ber.bool(true)
                        +Ber.int(42)
                    }.encode()

            val hex = sequence.toHexString()
            hex shouldBe "3080" + "0101FF" + "02012A" + "0000"
        }

        test("encode SET with unbounded length") {
            val set =
                Ber
                    .set {
                        unbounded() // Enable unbounded length encoding
                        +Ber.utf8String("Test")
                        +Ber.nullValue()
                    }.encode()

            val hex = set.toHexString()
            hex.startsWith("3180") shouldBe true // SET tag + indefinite length
            hex.endsWith("0000") shouldBe true // End-of-contents marker
            hex shouldContain "0C0454657374" // UTF8String "Test"
            hex shouldContain "0500" // NULL
        }

        test("encode nested structures with unbounded length") {
            val complex =
                Ber
                    .sequence {
                        unbounded() // Enable unbounded length for outer SEQUENCE
                        +Ber.int(123)
                        +Ber.sequence {
                            unbounded() // Enable unbounded length for inner SEQUENCE
                            +Ber.utf8String("Inner")
                            +Ber.nullValue()
                        }
                    }.encode()

            val hex = complex.toHexString()
            hex.startsWith("3080") shouldBe true // Outer sequence with indefinite length
            hex shouldContain "02017B" // INT 123
            hex shouldContain "3080" // Inner sequence with indefinite length
            hex shouldContain "0C05496E6E6572" // UTF8String "Inner"
            hex shouldContain "0500" // NULL

            // Two EOC markers (one for each unbounded sequence)
            val firstEoc = hex.indexOf("0000")
            val secondEoc = hex.lastIndexOf("0000")
            (firstEoc != secondEoc) shouldBe true
        }
    })

// Helper function to convert ByteArray to hex string
private fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }
