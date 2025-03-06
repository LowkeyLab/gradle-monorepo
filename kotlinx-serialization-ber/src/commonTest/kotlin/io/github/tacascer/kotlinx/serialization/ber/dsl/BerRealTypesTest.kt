package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.math.PI

@OptIn(ExperimentalStdlibApi::class)
class BerRealTypesTest :
    FunSpec({
        test("encode zero as real number") {
            val zero = Ber.real(0.0).encode()

            // Zero is encoded as empty content
            zero.toHexString() shouldBe "0900"
        }

        test("encode infinity") {
            val posInf = Ber.real(Double.POSITIVE_INFINITY).encode()
            posInf.toHexString() shouldBe "090140"

            val negInf = Ber.real(Double.NEGATIVE_INFINITY).encode()
            negInf.toHexString() shouldBe "090141"
        }

        test("encode NaN") {
            val nan = Ber.real(Double.NaN).encode()
            nan.toHexString() shouldBe "090142"
        }

        test("can encode positive real value") {
            val input = 10.0
            val sut = Ber.real(input)

            val encoded = sut.encode()

            encoded.toHexString() shouldBe "0903800105"
        }

        test("can encode negative real value") {
            val input = -10.0
            val sut = Ber.real(input)

            val encoded = sut.encode()

            encoded.toHexString() shouldBe "0903c00105"
        }

        test("include REAL value in a SEQUENCE") {
            val sequence =
                Ber
                    .sequence {
                        +Ber.utf8String("Value of PI:")
                        +Ber.real(PI)
                    }.encode()

            val hex = sequence.toHexString()

            // Should be a SEQUENCE (tag 0x30)
            hex.startsWith("30") shouldBe true

            // Should contain REAL tag (0x09)
            hex.contains("09") shouldBe true

            // Should contain UTF8STRING tag (0x0c)
            hex.contains("0c") shouldBe true
        }
    })
