package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.PI

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerRealTypesTest :
        FunSpec({
            test("encode real numbers should use proper BER encoding") {
                // Test with PI
                val piValue = Ber.Real(PI).encode()

                // Tag should be REAL (0x09)
                piValue[0] shouldBe 0x09.toByte()

                // Check that encoding follows BER format
                piValue.size shouldNotBe 0

                // First content byte should follow encoding rules:
                // For binary encoding, bit 8 = 1, bits 7-6 indicate base (00=2, 01=8, 10=16)
                // bit 5 = sign (0=positive, 1=negative), bits 4-3 = scaling factor

                val encodingByte = piValue[2].toInt() and 0xFF
                (encodingByte and 0x80) shouldBe 0x80 // Bit 8 should be set (binary encoding)

                // Base bits (bits 7-6)
                val base = (encodingByte and 0x60) shr 5
                base shouldBe 0 // Base 2 is encoded as 00

                // Sign bit (bit 5) - PI is positive
                (encodingByte and 0x10) shouldBe 0 // Sign bit should be 0 for positive
            }

            test("encode negative real number") {
                val negValue = Ber.Real(-PI).encode()

                // Tag should be REAL (0x09)
                negValue[0] shouldBe 0x09.toByte()

                // Check sign bit is set for negative
                val encodingByte = negValue[2].toInt() and 0xFF
                (encodingByte and 0x10) shouldNotBe 0 // Sign bit should be set for negative
            }

            test("encode zero as real number") {
                val zero = Ber.Real(0.0).encode()

                // Tag should be REAL (0x09)
                zero[0] shouldBe 0x09.toByte()

                // Zero is encoded as a special case - length should be 0
                zero[1] shouldBe 0x00.toByte()
            }

            test("real number encoding formula") {
                // In BER, REAL values are defined as Value = M * B^E
                // where M=mantissa, B=base, E=exponent
                // M is further broken down as M = S * N * 2^F
                // where S=sign, N=number, F=binary scaling factor

                // Test with a specific value that should use base 2 encoding
                val testValue = Ber.Real(12.5).encode() // 12.5 = 1.5625 * 2^3

                // Check basic encoding
                testValue[0] shouldBe 0x09.toByte() // REAL tag

                // Content should follow BER encoding rules
                val encodingByte = testValue[2].toInt() and 0xFF
                (encodingByte and 0x80) shouldBe 0x80 // Binary encoding

                // For base 2, check bits 7-6 are 00
                (encodingByte and 0x60) shouldBe 0x00 // Base 2

                // Sign bit should be 0 (positive)
                (encodingByte and 0x10) shouldBe 0x00
            }

            test("encode special values") {
                // For special values, BER has specific encodings

                // POSITIVE_INFINITY should have a specific encoding
                val posInf = Ber.Real(Double.POSITIVE_INFINITY).encode()
                posInf[0] shouldBe 0x09.toByte() // REAL tag
                posInf[1] shouldBe 0x01.toByte() // Length 1
                posInf[2] shouldBe 0x40.toByte() // 01000000 = positive infinity

                // NEGATIVE_INFINITY should have a specific encoding
                val negInf = Ber.Real(Double.NEGATIVE_INFINITY).encode()
                negInf[0] shouldBe 0x09.toByte() // REAL tag
                negInf[1] shouldBe 0x01.toByte() // Length 1
                negInf[2] shouldBe 0x41.toByte() // 01000001 = negative infinity

                // NaN should have a specific encoding
                val nan = Ber.Real(Double.NaN).encode()
                nan[0] shouldBe 0x09.toByte() // REAL tag
                nan[1] shouldBe 0x01.toByte() // Length 1
                nan[2] shouldBe 0x42.toByte() // 01000010 = not a number
            }

            test("real number with different bases") {
                // Base 2 (binary) encoding - default for most implementations
                val base2 = Ber.Real(6.25).encode() // 6.25 = 1.5625 * 2^2
                val encodingByte2 = base2[2].toInt() and 0xFF
                (encodingByte2 and 0x60) shouldBe 0x00 // Base 2 is bits 7-6 = 00

                // Note: Most implementations won't use base 8 or 16, but the BER standard allows it
                // We can only check that the value is properly encoded somehow
                val base8or16Value = Ber.Real(100000.0).encode()
                base8or16Value[0] shouldBe 0x09.toByte() // Still a REAL tag
            }

            test("include real in sequence") {
                val seq =
                        Ber.Sequence {
                                    +Ber.Real(1.234)
                                    +Ber.Int(42)
                                }
                                .encode()

                // Verify it's a sequence
                seq[0] shouldBe 0x30.toByte()

                // First element should be REAL
                seq[2] shouldBe 0x09.toByte()
            }

            test("encode simple integer value 10 as REAL") {
                val ten = Ber.Real(10.0).encode()

                // Tag should be REAL (0x09)
                ten[0] shouldBe 0x09.toByte()

                // Length should be at least 3 bytes (1 for format, 1 for exponent, 1+ for mantissa)
                ten[1] shouldBe 0x03.toByte()

                // First content byte should indicate binary encoding
                val formatByte = ten[2].toInt() and 0xFF
                (formatByte and 0x80) shouldBe 0x80 // Binary encoding flag

                // For 10 = 1.25 * 2^3, using binary encoding with base 2:
                // - Base should be 2 (bits 7-6 = 00)
                (formatByte and 0x60) shouldBe 0x00

                // - Sign should be positive (bit 5 = 0)
                (formatByte and 0x10) shouldBe 0x00

                // The exponent should be 3
                // In two's complement, positive 3 is just 0x03
                ten[3] shouldBe 0x03.toByte()

                // The mantissa for 1.25 normalized is 0x0A (decimal 10)
                // or in a different normalization could be 0x05 with scaling
                // Let's check the mantissa is reasonable (can be different implementations)
                val mantissa = ten[4].toInt() and 0xFF

                // For 10, mantissa values can vary depending on normalization
                // Common values:
                // - 0x05 (5) if normalized as 1.25 * 2^3
                // - 0x0A (10) if just encoded as integer
                // Our implementation should produce some valid encoding
                (mantissa == 0x05 || mantissa == 0x0A) shouldBe true
            }
        })
