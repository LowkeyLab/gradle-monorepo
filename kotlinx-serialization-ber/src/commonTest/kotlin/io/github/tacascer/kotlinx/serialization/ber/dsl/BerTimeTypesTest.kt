package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BerTimeTypesTest :
    FunSpec({
        // Test UTC Time (YYMMDDhhmmssZ format)
        context("UTC Time encoding") {
            test("encode current time as UTCTime") {
                val now = Clock.System.now()
                val encoded = Ber.utcTime(now).encode()
                val hex = encoded.toHexString()

                // UTCTime tag (0x17) followed by length
                hex.startsWith("17") shouldBe true

                // Convert the content bytes back to a string
                val contentBytes = encoded.sliceArray(2 until encoded.size)
                val timeString = contentBytes.decodeToString()

                // Format should be YYMMDDhhmmssZ with two-digit year
                timeString.length shouldBe 13
                timeString[timeString.lastIndex] shouldBe 'Z'

                // Extract encoded date/time components and compare to original
                val localDt = now.toLocalDateTime(TimeZone.UTC)
                val year = timeString.substring(0, 2).toInt()
                val month = timeString.substring(2, 4).toInt()
                val day = timeString.substring(4, 6).toInt()
                val hour = timeString.substring(6, 8).toInt()
                val minute = timeString.substring(8, 10).toInt()
                val second = timeString.substring(10, 12).toInt()

                // Verify components match (note: year is only two digits in UTCTime)
                year shouldBe (localDt.year % 100)
                month shouldBe localDt.monthNumber
                day shouldBe localDt.dayOfMonth
                hour shouldBe localDt.hour
                minute shouldBe localDt.minute
                second shouldBe localDt.second
            }

            test("encode specific UTC time") {
                // Choose a specific datetime for consistent testing
                val specificTime = Instant.parse("2023-04-15T20:30:45Z")
                val encoded = Ber.utcTime(specificTime).encode()
                val hex = encoded.toHexString()

                // Expected: tag(17) + length(13) + "230415203045Z" as ASCII
                hex shouldBe "170d3233303431353230333034355a"
            }
        }

        // Test Generalized Time (YYYYMMDDhhmmss[.f]Z format)
        context("Generalized Time encoding") {
            test("encode current time as GeneralizedTime") {
                val now = Clock.System.now()
                val encoded = Ber.generalizedTime(now).encode()
                val hex = encoded.toHexString()

                // Generalized Time has tag 0x18
                hex.startsWith("18") shouldBe true

                // Convert the content bytes back to a string
                val contentBytes = encoded.sliceArray(2 until encoded.size)
                val timeString = contentBytes.decodeToString()

                // Should end with Z for UTC
                timeString[timeString.lastIndex] shouldBe 'Z'

                // Should contain four-digit year
                val yearStr = timeString.substring(0, 4)
                yearStr.length shouldBe 4
            }

            test("encode Generalized Time with milliseconds") {
                // Choose a specific datetime with milliseconds
                val timeWithMillis = Instant.parse("2023-04-15T20:30:45.123Z")
                val encoded = Ber.generalizedTime(timeWithMillis).encode()

                // Convert content to string
                val contentBytes = encoded.sliceArray(2 until encoded.size)
                val timeString = contentBytes.decodeToString()

                // Should include milliseconds
                timeString shouldContain "."
            }

            test("encode specific GeneralizedTime example - fileCreated") {
                // Test the specific example from the prompt: "19960415203000.0"
                val fileCreatedTime = Instant.parse("1996-04-15T20:30:00Z")
                val encoded = Ber.generalizedTime(fileCreatedTime).encode()
                val hex = encoded.toHexString()

                // Generalized Time tag
                hex.startsWith("18") shouldBe true

                // The content should represent the date 1996-04-15T20:30:00
                val contentBytes = encoded.sliceArray(2 until encoded.size)
                val timeString = contentBytes.decodeToString()

                timeString shouldContain "1996"
                timeString shouldContain "0415" // April 15
                timeString shouldContain "2030" // 20:30
            }
        }

        // Testing time types in structures
        context("Time types in BER structures") {
            test("include times in SEQUENCE") {
                val now = Clock.System.now()
                val sequence =
                    Ber
                        .sequence {
                            +Ber.utf8String("Created:")
                            +Ber.utcTime(now)
                            +Ber.utf8String("Updated:")
                            +Ber.generalizedTime(now)
                        }.encode()

                val hex = sequence.toHexString()

                // Sequence tag followed by content
                hex.startsWith("30") shouldBe true

                // Should contain both UTC Time and Generalized Time tags
                hex shouldContain "17" // UTCTime tag
                hex shouldContain "18" // GeneralizedTime tag
            }

            test("encode time range with start and end times") {
                val start = Instant.parse("2023-01-01T00:00:00Z")
                val end = Instant.parse("2023-12-31T23:59:59Z")

                val timeRange =
                    Ber
                        .sequence {
                            +Ber.sequence {
                                +Ber.utf8String("validity")
                                +Ber.utcTime(start)
                                +Ber.utcTime(end)
                            }
                        }.encode()

                val hex = timeRange.toHexString()

                // SEQUENCE tag
                hex.startsWith("30") shouldBe true

                // Count UTCTime tags
                val tagIndices =
                    hex.indices.filter { i ->
                        i + 1 < hex.length && hex.substring(i, i + 2) == "17"
                    }
                tagIndices.size shouldBe 2
            }
        }
    })
