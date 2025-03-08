package io.github.lowkeylab.ber.builder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerStringTypesTest :
    FunSpec({

        // Test UTF8String encoding
        context("UTF8String encoding") {
            test("encode basic ASCII string as UTF8String") {
                val basicString = "Hello, world!"
                val encoded = Ber.utf8String(basicString).encode()

                // UTF8String has tag 0x0C
                encoded[0] shouldBe 0x0C.toByte()

                // Length should match string length
                encoded[1] shouldBe basicString.length.toByte()

                // Convert content back to string and verify
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe basicString
            }

            test("encode UTF8String with Unicode characters") {
                val unicodeString = "こんにちは世界" // "Hello world" in Japanese
                val encoded = Ber.utf8String(unicodeString).encode()

                // Tag should be UTF8String
                encoded[0] shouldBe 0x0C.toByte()

                // Content should decode back to the original string
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe unicodeString
            }

            test("encode empty UTF8String") {
                val emptyString = ""
                val encoded = Ber.utf8String(emptyString).encode()

                // Tag should be UTF8String
                encoded[0] shouldBe 0x0C.toByte()

                // Length should be 0
                encoded[1] shouldBe 0x00.toByte()

                // Should have just tag and length (2 bytes total)
                encoded.size shouldBe 2
            }
        }

        // Test PrintableString encoding
        context("PrintableString encoding") {
            test("encode basic PrintableString") {
                val printableString = "Hello123!"
                val encoded = Ber.printableString(printableString).encode()

                // PrintableString has tag 0x13
                encoded[0] shouldBe 0x13.toByte()

                // Length should match string length
                encoded[1] shouldBe printableString.length.toByte()

                // Content should decode back to the original string
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe printableString
            }

            // PrintableString is limited to a subset of ASCII
            test("verify printable string content") {
                // This is a valid PrintableString according to ASN.1
                val validString =
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 '()+,-./:=?"
                val encoded = Ber.printableString(validString).encode()

                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe validString
            }
        }

        // Test NumericString encoding
        context("NumericString encoding") {
            test("encode basic NumericString") {
                val numericString = "12345 67890" // Digits and spaces only
                val encoded = Ber.numericString(numericString).encode()

                // NumericString has tag 0x12
                encoded[0] shouldBe 0x12.toByte()

                // Length should match string length
                encoded[1] shouldBe numericString.length.toByte()

                // Content should decode back to the original string
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe numericString
            }
        }

        // Test IA5String encoding (ASCII)
        context("IA5String encoding") {
            test("encode basic IA5String") {
                val asciiString = "Hello, World! 123"
                val encoded = Ber.ia5String(asciiString).encode()

                // IA5String has tag 0x16
                encoded[0] shouldBe 0x16.toByte()

                // Length should match string length
                encoded[1] shouldBe asciiString.length.toByte()

                // Content should decode back to the original string
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe asciiString
            }

            test("encode IA5String with control characters") {
                // IA5 includes control characters like tab, newline
                val controlChars = "Line1\nLine2\tTabbed"
                val encoded = Ber.ia5String(controlChars).encode()

                // Content should decode back to the original string
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedString = content.decodeToString()
                decodedString shouldBe controlChars
            }
        }

        // Test BMPString encoding (UCS-2)
        context("BMPString encoding") {
            test("encode basic BMPString with ASCII") {
                val basicString = "Hello"
                val encoded = Ber.bmpString(basicString).encode()

                // BMPString has tag 0x1E
                encoded[0] shouldBe 0x1E.toByte()

                // Length should be 2x string length (UCS-2 uses 2 bytes per character)
                encoded[1] shouldBe (basicString.length * 2).toByte()

                // For ASCII, first byte of each character should be 0, second byte should be
                // ASCII value
                val content = encoded.sliceArray(2 until encoded.size)
                for (i in basicString.indices) {
                    content[i * 2] shouldBe 0x00.toByte() // High byte (0 for ASCII)
                    content[i * 2 + 1] shouldBe
                        basicString[i].code.toByte() // Low byte (ASCII value)
                }
            }

            test("encode BMPString with Unicode characters") {
                val unicodeString = "Hello → World" // With non-ASCII character
                val encoded = Ber.bmpString(unicodeString).encode()

                // BMPString has tag 0x1E
                encoded[0] shouldBe 0x1E.toByte()

                // Length should be 2x string length (UCS-2 uses 2 bytes per character)
                encoded[1] shouldBe (unicodeString.length * 2).toByte()

                // Manually decode UCS-2 big endian
                val content = encoded.sliceArray(2 until encoded.size)
                val decodedChars = CharArray(content.size / 2)
                for (i in decodedChars.indices) {
                    val highByte = content[i * 2].toInt() and 0xFF
                    val lowByte = content[i * 2 + 1].toInt() and 0xFF
                    val charCode = (highByte shl 8) or lowByte
                    decodedChars[i] = charCode.toChar()
                }

                String(decodedChars) shouldBe unicodeString
            }
        }

        // Test string types in BER structures
        context("String types in BER structures") {
            test("include various string types in SEQUENCE") {
                val sequence =
                    Ber
                        .sequence {
                            +Ber.utf8String("UTF8")
                            +Ber.printableString("Printable123")
                            +Ber.numericString("12345")
                            +Ber.ia5String("IA5@example.com")
                            +Ber.bmpString("BMP♥String")
                        }.encode()

                // Verify it's a sequence
                sequence[0] shouldBe 0x30.toByte()

                // Check it contains all string type tags
                val bytes = sequence.toList()
                bytes.contains(0x0C.toByte()) shouldBe true // UTF8String
                bytes.contains(0x13.toByte()) shouldBe true // PrintableString
                bytes.contains(0x12.toByte()) shouldBe true // NumericString
                bytes.contains(0x16.toByte()) shouldBe true // IA5String
                bytes.contains(0x1E.toByte()) shouldBe true // BMPString
            }

            test("encode strings with large content") {
                // Create a string that will exceed 127 bytes in UTF-8
                val longString = "A".repeat(200)
                val encoded = Ber.utf8String(longString).encode()

                // For lengths >= 128, length encoding becomes multi-byte
                // First byte should have high bit set and indicate number of length bytes
                encoded[0] shouldBe 0x0C.toByte() // UTF8String tag
                (encoded[1].toInt() and 0x80) shouldBe 0x80 // High bit set

                // Decode the length and verify
                val numLengthBytes = encoded[1].toInt() and 0x7F
                var length = 0
                for (i in 0 until numLengthBytes) {
                    length = (length shl 8) or (encoded[i + 2].toInt() and 0xFF)
                }

                length shouldBe longString.length
            }
        }
    })
