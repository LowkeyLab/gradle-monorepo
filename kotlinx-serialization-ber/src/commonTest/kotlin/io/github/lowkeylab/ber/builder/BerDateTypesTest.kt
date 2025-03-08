package io.github.lowkeylab.ber.builder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerDateTypesTest :
    FunSpec({
        test("encode DATE value 2012-12-21") {
            // Define the date
            val endOfTime = "2012-12-21"

            // Encode using our custom DATE builder
            val encoded = Ber.date(endOfTime).encode()

            // Expected bytes: 1F 1F 08 32 30 31 32 31 32 32 31
            val expected =
                byteArrayOf(
                    0x1F,
                    0x1F, // Tag for DATE (31 in extended form)
                    0x08, // Length of content (8 bytes)
                    0x32,
                    0x30,
                    0x31,
                    0x32, // ASCII for "2012"
                    0x31,
                    0x32,
                    0x32,
                    0x31, // ASCII for "1221"
                )

            // Convert to hex strings for easier debugging
            val encodedHex = encoded.joinToString("") { "%02X".format(it) }
            val expectedHex = expected.joinToString("") { "%02X".format(it) }

            println("Encoded: $encodedHex")
            println("Expected: $expectedHex")

            // Verify the encoded bytes match the expected bytes
            encoded shouldBe expected
        }

        test("encode DATE value and include in SEQUENCE") {
            // Encode a sequence with a DATE value inside
            val sequence =
                Ber
                    .sequence {
                        +Ber.date("2012-12-21")
                        +Ber.printableString("End of Time")
                    }.encode()

            // Verify it starts with a SEQUENCE tag
            sequence[0] shouldBe 0x30.toByte() // SEQUENCE tag

            // Verify the DATE element is inside with tag 1F 1F
            val dateTagPosition =
                sequence.indexOfSequence(byteArrayOf(0x1F.toByte(), 0x1F.toByte()))
            dateTagPosition shouldBe
                2 // Should be at position 2 (after SEQUENCE tag and length)

            // Verify the content length for DATE is 8
            sequence[dateTagPosition + 2] shouldBe 0x08.toByte()
        }
    })

// Helper function to find a sequence of bytes within another byte array
fun ByteArray.indexOfSequence(sequence: ByteArray): Int {
    outer@ for (i in 0..this.size - sequence.size) {
        for (j in sequence.indices) {
            if (this[i + j] != sequence[j]) continue@outer
        }
        return i
    }
    return -1
}
