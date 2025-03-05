package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import io.github.tacascer.kotlinx.serialization.ber.BerTaggedElement

/** Builder for implicitly tagged elements */
internal class BerImplicitlyTaggedBuilder(
    private val inner: BerBuilder,
    private val tagNumber: Long,
    private val tagClass: BerTagClass,
    private val constructed: Boolean,
) : BerBuilder,
    BerTaggedElement {
    override fun encode(): ByteArray {
        // For an implicit tag, we get the content of the inner element
        // but ignore its tag and use our own
        val innerBytes = inner.encode()

        // Skip the tag and length of the inner element
        var i = 0
        val tag = innerBytes[i++].toInt() and 0xFF

        // Skip over multi-byte tags
        if ((tag and 0x1F) == 31) {
            while (i < innerBytes.size && (innerBytes[i++].toInt() and 0x80) != 0) {
                // Just iterate through each byte with the high bit set
            }
        }

        // Skip over the length
        val lenByte = innerBytes[i++].toInt() and 0xFF
        if ((lenByte and 0x80) != 0) {
            val numLenOctets = lenByte and 0x7F
            i += numLenOctets
        }

        // Get the content bytes
        val contentBytes = innerBytes.copyOfRange(i, innerBytes.size)

        // Create new element with our tag but the inner content
        return BerInternalUtils.encodeBerElement(tagClass, tagNumber, constructed, contentBytes)
    }

    override fun getTag(): BerTag {
        // Using custom tag number, not a standard BerTag enum value
        return BerTag.NULL // This is a placeholder
    }

    override fun getTagClass(): BerTagClass = tagClass
}

/** Builder for explicitly tagged elements */
internal class BerExplicitlyTaggedBuilder(
    private val inner: BerBuilder,
    private val tagNumber: Long,
    private val tagClass: BerTagClass,
) : BerBuilder,
    BerTaggedElement {
    override fun encode(): ByteArray {
        // For explicit tagging, we include the entire inner element
        val innerBytes = inner.encode()

        // Create new element with our tag and the entire inner element as content
        return BerInternalUtils.encodeBerElement(tagClass, tagNumber, true, innerBytes)
    }

    override fun getTag(): BerTag {
        // Using custom tag number, not a standard BerTag enum value
        return BerTag.NULL // This is a placeholder
    }

    override fun getTagClass(): BerTagClass = tagClass
}
