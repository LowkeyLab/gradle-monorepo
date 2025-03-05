package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerElement
import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** UTC TIME type builder (YYMMDDhhmmssZ) */
internal class BerUtcTimeBuilder(
    private val instant: Instant,
) : BerPrimitiveBuilder(),
    BerElement {
    override fun encode(): ByteArray {
        val dt = instant.toLocalDateTime(TimeZone.UTC)
        val year = dt.year % 100 // Two digit year
        val month = dt.monthNumber
        val day = dt.dayOfMonth
        val hour = dt.hour
        val minute = dt.minute
        val second = dt.second

        val timeString =
            String.format("%02d%02d%02d%02d%02d%02dZ", year, month, day, hour, minute, second)

        val content = timeString.encodeToByteArray()
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.UTC_TIME, content)
    }

    override fun getTag() = BerTag.UTC_TIME

    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** GENERALIZED TIME type builder (YYYYMMDDHHMMSS.sssZ) */
internal class BerGeneralizedTimeBuilder(
    private val instant: Instant,
) : BerPrimitiveBuilder(),
    BerElement {
    override fun encode(): ByteArray {
        val dt = instant.toLocalDateTime(TimeZone.UTC)
        val nano = dt.nanosecond / 1_000_000 // Convert to milliseconds

        val timeString =
            if (nano == 0) {
                String.format(
                    "%04d%02d%02d%02d%02d%02dZ",
                    dt.year,
                    dt.monthNumber,
                    dt.dayOfMonth,
                    dt.hour,
                    dt.minute,
                    dt.second,
                )
            } else {
                String.format(
                    "%04d%02d%02d%02d%02d%02d.%03dZ",
                    dt.year,
                    dt.monthNumber,
                    dt.dayOfMonth,
                    dt.hour,
                    dt.minute,
                    dt.second,
                    nano,
                )
            }

        val content = timeString.encodeToByteArray()
        return BerInternalUtils.encodeBerElement(
            BerTagClass.UNIVERSAL,
            BerTag.GENERALIZED_TIME,
            content,
        )
    }

    override fun getTag() = BerTag.GENERALIZED_TIME

    override fun getTagClass() = BerTagClass.UNIVERSAL
}
