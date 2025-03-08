package io.github.lowkeylab.ber.builder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@OptIn(ExperimentalStdlibApi::class)
class BerDateTypesTest :
    FunSpec({
        test("encode DATE value 2012-12-21") {
            // Define the date
            val endOfTime = "2012-12-21"

            // Encode using our custom DATE builder
            val encoded = Ber.date(endOfTime).encode()

            // Expected hex representation of DATE encoding
            val expectedHex = "1f1f083230313231323231"

            encoded.toHexString() shouldBe expectedHex
        }

        test("encode DATE value and include in SEQUENCE") {
            // Encode a sequence with a DATE value inside
            val sequence =
                Ber
                    .sequence {
                        +Ber.date("2012-12-21")
                        +Ber.printableString("End of Time")
                    }.encode()

            val sequenceHex = sequence.toHexString()

            // Verify it starts with a SEQUENCE tag
            sequenceHex.startsWith("30") shouldBe true

            // Verify the DATE element is inside with tag 1F 1F
            sequenceHex shouldContain "1f1f" // DATE tag

            // Check position of DATE tag in the hex string (position 2 in bytes = position 4 in hex)
            val dateTagPosition = sequenceHex.indexOf("1f1f")
            dateTagPosition shouldBe 4 // After SEQUENCE tag and length

            // Verify the content length for DATE is 8
            sequenceHex.substring(dateTagPosition + 4, dateTagPosition + 6) shouldBe "08"

            // Verify date content is included
            sequenceHex shouldContain "3230313231323231" // "20121221" in hex
        }
    })
