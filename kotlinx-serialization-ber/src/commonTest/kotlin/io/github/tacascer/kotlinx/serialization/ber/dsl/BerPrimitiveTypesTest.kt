package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerPrimitiveTypesTest :
        FunSpec({
            test("encode Boolean values correctly") {
                // Boolean TRUE - explicitly convert to hex for verification
                val trueBytes = Ber.Bool(true).encode()
                trueBytes.toHexString() shouldBe "0101ff"

                // Boolean FALSE
                val falseBytes = Ber.Bool(false).encode()
                falseBytes.toHexString() shouldBe "010100"
            }

            // Update other tests to use the same pattern
            test("encode Integer values correctly") {
                // Small positive integer
                val int42 = Ber.Int(42).encode()
                int42.toHexString() shouldBe "02012a"

                // Large positive integer
                val int65535 = Ber.Int(65535).encode()
                int65535.toHexString() shouldBe "020300ffff"

                // Negative integer
                val intNeg42 = Ber.Int(-42).encode()
                intNeg42.toHexString() shouldBe "0201d6"

                // Long integer
                val longValue = Ber.Int(1234567890123456789L).encode()
                longValue.toHexString() shouldBe "02091121f494a6f7b75bcd15"
            }

            test("encode Null value correctly") {
                val nullValue = Ber.Null().encode()
                nullValue.toHexString() shouldBe "0500"
            }

            test("encode ObjectIdentifier values correctly") {
                val oid = Ber.ObjectIdentifier("1.2.840.113549.1.1.11").encode() // SHA256withRSA
                oid.toHexString() shouldBe "06092a864886f70d01010b"

                val oid2 = Ber.ObjectIdentifier("2.5.4.3").encode() // Common Name
                oid2.toHexString() shouldBe "0603550403"
            }

            test("encode UTF8String correctly") {
                val utf8 = Ber.Utf8String("Hello, world!").encode()
                utf8.toHexString() shouldBe "0c0d48656c6c6f2c20776f726c6421"
            }

            test("encode PrintableString correctly") {
                val printable = Ber.PrintableString("Test123").encode()
                printable.toHexString() shouldBe "130754657374313233"
            }

            test("encode OctetString correctly") {
                val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
                val octetString = Ber.OctetString(bytes).encode()
                octetString.toHexString() shouldBe "04050102030405"
            }

            test("encode BMPString correctly") {
                val bmpString = Ber.BMPString("AB").encode() // Simple ASCII chars in BMP
                bmpString.toHexString() shouldBe
                        "1e0400410042" // 00 41 = 'A', 00 42 = 'B' in UTF-16BE
            }

            test("encode complex nested structure") {
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

            test("encode tagged values correctly") {
                // [0] IMPLICIT UTF8String
                val implicitTag =
                        (Ber.Utf8String("Tagged") withImplicitTag
                                        (0uL withClass BerTagClass.CONTEXT_SPECIFIC))
                                .encode()
                implicitTag[0] shouldBe 0x80.toByte() // [0] tag

                // [1] EXPLICIT SEQUENCE { UTF8String }
                val explicitTag = ExplicitlyTagged(1uL) { +Ber.Utf8String("Inside Tag") }.encode()

                explicitTag[0] shouldBe 0xA1.toByte() // [1] EXPLICIT
                // Should contain a UTF8String inside
                (explicitTag.sliceArray(2 until explicitTag.size).any {
                    it == 0x0C.toByte()
                }) shouldBe true
            }
        })
