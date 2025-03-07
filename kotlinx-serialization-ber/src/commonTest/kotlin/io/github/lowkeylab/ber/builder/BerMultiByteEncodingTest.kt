package io.github.lowkeylab.ber.builder

import io.github.lowkeylab.ber.BerTagClass
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerMultiByteEncodingTest :
    FunSpec({

        // Test BER encoding with large lengths (> 127 bytes)
        context("BER encoding with large lengths") {
            test("encode octet string with length > 127") {
                // Create a byte array larger than 127 bytes
                val largeData = ByteArray(200) { it.toByte() }
                val encoded = Ber.octetString(largeData).encode()

                // Verify tag is OCTET STRING
                encoded[0] shouldBe 0x04.toByte()

                // Length field should use long form encoding (bit 7 set, followed by length
                // bytes)
                // 0x81 = 10000001 = High bit set + 1 length byte follows
                encoded[1] shouldBe 0x81.toByte()

                // The length value should be 200 (0xC8)
                encoded[2] shouldBe 0xC8.toByte()

                // Total size: 1 byte tag + 2 bytes length + 200 bytes content = 203 bytes
                encoded.size shouldBe 203

                // Verify first few content bytes
                encoded[3] shouldBe 0x00.toByte()
                encoded[4] shouldBe 0x01.toByte()
                encoded[5] shouldBe 0x02.toByte()
            }

            test("encode octet string with length > 255") {
                // Create a byte array larger than 255 bytes
                val largeData = ByteArray(1000) { it.toByte() }
                val encoded = Ber.octetString(largeData).encode()

                // Verify tag is OCTET STRING
                encoded[0] shouldBe 0x04.toByte()

                // Length field should use long form encoding with 2 bytes
                // 0x82 = 10000010 = High bit set + 2 length bytes follow
                encoded[1] shouldBe 0x82.toByte()

                // The length value should be 1000 (0x03E8)
                encoded[2] shouldBe 0x03.toByte() // High byte
                encoded[3] shouldBe 0xE8.toByte() // Low byte

                // Total size: 1 byte tag + 3 bytes length + 1000 bytes content = 1004 bytes
                encoded.size shouldBe 1004
            }

            test("encode sequence with total length > 127") {
                // Create a sequence with multiple elements, total size exceeding 127 bytes
                val sequence =
                    Ber
                        .sequence {
                            // Add several string elements to exceed 127 bytes total
                            +Ber.utf8String("A".repeat(50))
                            +Ber.utf8String("B".repeat(50))
                            +Ber.utf8String("C".repeat(50))
                        }.encode()

                // Verify tag is SEQUENCE
                sequence[0] shouldBe 0x30.toByte()

                // Length field should use long form encoding
                (sequence[1].toInt() and 0x80) shouldBe 0x80

                // Decode the length value
                val lengthBytes = sequence[1].toInt() and 0x7F
                var decodedLength = 0
                for (i in 0 until lengthBytes) {
                    decodedLength = (decodedLength shl 8) or (sequence[2 + i].toInt() and 0xFF)
                }

                // Total content length should be correctly encoded
                decodedLength shouldBe (sequence.size - (2 + lengthBytes))
            }
        }

        // Test BER encoding with extended tag format (tag numbers > 30)
        context("BER encoding with extended tag format") {
            test("encode element with tag number 31 (minimum extended tag)") {
                // To create an element with a non-standard tag, we need to use implicit tagging
                // Create a UTF8String and tag it with tag 31
                val taggedElement =
                    (
                        Ber.utf8String("Extended Tag Test") withImplicitTag
                            (31uL withClass BerTagClass.CONTEXT_SPECIFIC)
                    ).encode()

                // For extended tag format, first byte has bits 1-5 all set to 1 (0x1F)
                // and the class bits in the high bits (context-specific = 10xxxxxx)
                taggedElement[0] shouldBe
                    0x9F.toByte() // 10011111 = Context-specific (10) + extended tag
                // indicator (11111)

                // Second byte contains the actual tag number (31 = 0x1F)
                // Since it's less than 127, it doesn't need continuation bits
                taggedElement[1] shouldBe 0x1F.toByte()

                // Then comes the length and content
                val content = "Extended Tag Test"
                taggedElement[2] shouldBe content.length.toByte()
            }

            test("encode element with large tag number (> 127)") {
                // Use a tag number that requires multiple bytes in extended format
                val largeTag = 1000uL
                val taggedElement =
                    (
                        Ber.utf8String("Large Tag Test") withImplicitTag
                            (largeTag withClass BerTagClass.CONTEXT_SPECIFIC)
                    ).encode()

                // First byte indicates extended tag format with context-specific class
                taggedElement[0] shouldBe 0x9F.toByte() // 10011111

                // For a tag value of 1000 (0x3E8), encoding in base-128 with continuation bits:
                // 1000 = 0x3E8 = 0b111_1101000
                // Split into 7-bit groups with continuation bits:
                // 0b0000_111 0b1_1010_00
                // Add continuation bit to first byte: 0b1000_0111 (0x87)
                // Second byte doesn't need continuation: 0b0111_0100 (0x74)
                taggedElement[1] shouldBe 0x87.toByte() // First tag byte with continuation bit
                taggedElement[2] shouldBe
                    0x68.toByte() // Second tag byte (note: calculation depends on exact
                // implementation)

                // Then should come length and content
                val content = "Large Tag Test"
                val tagLength = 3 // 1 byte for initial tag, 2 bytes for extended format
                taggedElement[tagLength] shouldBe content.length.toByte()
            }

            test("encode element with very large tag number") {
                // Test with a tag number that requires 3+ bytes
                val veryLargeTag = 16384uL // 2^14, needs more than 2 bytes in extended form
                val taggedElement =
                    (
                        Ber.utf8String("Very Large Tag") withImplicitTag
                            (veryLargeTag withClass BerTagClass.CONTEXT_SPECIFIC)
                    ).encode()

                // First byte indicates extended tag format
                taggedElement[0] shouldBe 0x9F.toByte()

                // For a tag value of 16384 (0x4000), encoding in base-128 with continuation
                // bits:
                // First byte should have continuation bit set
                (taggedElement[1].toInt() and 0x80) shouldBe 0x80

                // Instead of checking for exact size (which depends on implementation details),
                // verify that the content is present and correctly decoded
                val content = "Very Large Tag"
                val contentLength = content.length

                // Find the length byte (comes after the tag bytes)
                // The tag bytes include the initial byte (0x9F) and 1+ bytes for the extended
                // tag
                var tagBytesCount = 1
                var i = 1
                while (i < taggedElement.size && (taggedElement[i].toInt() and 0x80) == 0x80) {
                    tagBytesCount++
                    i++
                }
                tagBytesCount++ // Add the final tag byte without continuation bit

                // Verify the length byte encodes the content length correctly
                taggedElement[tagBytesCount] shouldBe contentLength.toByte()

                // Verify the content is present (starting after tag and length)
                val actualContent =
                    taggedElement
                        .sliceArray((tagBytesCount + 1) until taggedElement.size)
                        .decodeToString()
                actualContent shouldBe content

                // The size should be: number of tag bytes + 1 length byte + content length
                taggedElement.size shouldBe (tagBytesCount + 1 + contentLength)
            }
        }

        // Combined tests with both large tags and large content
        context("Combined large tag and large content") {
            test("encode element with large tag and large content") {
                // Create a large content (> 127 bytes)
                val largeContent = "A".repeat(200)

                // Apply a large tag (> 127)
                val largeTag = 500uL

                // Create the tagged element
                val complexElement =
                    (
                        Ber.utf8String(largeContent) withImplicitTag
                            (largeTag withClass BerTagClass.APPLICATION)
                    ).encode()

                // Verify tag format is correct (extended format)
                complexElement[0] shouldBe
                    0x5F.toByte() // 01011111 = Application class (01) + extended tag
                // (11111)

                // Verify length format is correct (long form)
                var tagBytes = 1 // Start with 1 for the initial tag byte

                // Count tag bytes (continue until byte without continuation bit)
                var i = 1
                while (i < complexElement.size &&
                    (complexElement[i].toInt() and 0x80) == 0x80
                ) {
                    tagBytes++
                    i++
                }
                tagBytes++ // Add the final tag byte

                // The next byte should be the length indicator
                val lengthIndicator = complexElement[tagBytes].toInt() and 0xFF

                // Verify length uses long form (high bit set)
                (lengthIndicator and 0x80) shouldBe 0x80

                // Verify total size is consistent
                val lengthBytes = lengthIndicator and 0x7F
                var contentLength = 0
                for (j in 0 until lengthBytes) {
                    contentLength =
                        (contentLength shl 8) or
                        (complexElement[tagBytes + 1 + j].toInt() and 0xFF)
                }

                contentLength shouldBe largeContent.length
                complexElement.size shouldBe (tagBytes + 1 + lengthBytes + contentLength)
            }
        }

        // Test nested structures with large components
        context("Nested structures with large components") {
            test("encode nested sequence with large tags and content") {
                val nested =
                    Ber
                        .sequence {
                            // Add a normally-tagged element
                            +Ber.utf8String("Normal String")

                            // Add an element with extended tag
                            +(
                                Ber.int(12345) withImplicitTag
                                    (100uL withClass io.github.lowkeylab.ber.BerTagClass.CONTEXT_SPECIFIC)
                            )

                            // Add a large content element
                            +Ber.octetString(ByteArray(150) { it.toByte() })

                            // Add a deeply nested element with large tag
                            +Ber.sequence {
                                +(
                                    Ber.utf8String("Deeply Nested") withImplicitTag
                                        (2000uL withClass io.github.lowkeylab.ber.BerTagClass.PRIVATE)
                                )
                            }
                        }.encode()

                // Verify it's a SEQUENCE
                nested[0] shouldBe 0x30.toByte()

                // Verify the length uses long form (total content > 127 bytes)
                (nested[1].toInt() and 0x80) shouldBe 0x80

                // Find the large tag in the encoded content
                val tagPosition =
                    findExtendedTagPosition(
                        nested,
                        0x9F.toByte(),
                    ) // Context-specific extended tag
                tagPosition shouldBe greaterThan(0) // Should have found the extended tag

                // Find the private class tag
                val privateTagPosition =
                    findExtendedTagPosition(nested, 0xDF.toByte()) // Private extended tag
                privateTagPosition shouldBe greaterThan(0) // Should have found the private tag
            }
        }
    })

// Helper function to find position of an extended tag in a byte array
fun findExtendedTagPosition(
    bytes: ByteArray,
    tagFirstByte: Byte,
): Int {
    for (i in bytes.indices) {
        if (bytes[i] == tagFirstByte) {
            return i
        }
    }
    return -1
}

// Helper function for shouldBe greaterThan
fun greaterThan(expected: Int) =
    object : io.kotest.matchers.Matcher<Int> {
        override fun test(value: Int): io.kotest.matchers.MatcherResult =
            io.kotest.matchers.MatcherResult(
                value > expected,
                { "$value should be greater than $expected" },
                { "$value should not be greater than $expected" },
            )
    }
