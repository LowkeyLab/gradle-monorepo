package io.github.lowkeylab.ber.builder

import io.github.lowkeylab.ber.BerElement
import io.github.lowkeylab.ber.BerSequence
import io.github.lowkeylab.ber.BerSet
import io.github.lowkeylab.ber.BerSetOf
import io.github.lowkeylab.ber.BerTag
import io.github.lowkeylab.ber.BerTagClass

/** Base class for constructed BER elements */
internal abstract class BerConstructedBuilder : BerBuilder {
    val children = mutableListOf<BerElement>()
    var useUnboundedLength = false

    /** Add a child builder */
    operator fun BerBuilder.unaryPlus() {
        children.add(this)
    }

    /** Enable unbounded length encoding */
    fun unbounded() {
        useUnboundedLength = true
    }
}

/** SEQUENCE type builder */
internal class BerSequenceBuilder :
    BerConstructedBuilder(),
    BerSequence {
    override fun encode(): ByteArray {
        // First encode children to get their content
        val childrenBytes = children.flatMap { it.encode().toList() }

        // Create new element with SEQUENCE tag and children as content
        return BerInternalUtils.encodeBerElement(
            BerTagClass.UNIVERSAL,
            BerTag.SEQUENCE,
            childrenBytes.toByteArray(),
            useUnboundedLength,
        )
    }

    override operator fun BerElement.unaryPlus() {
        children.add(this)
    }
}

/** SET type builder */
internal class BerSetBuilder :
    BerConstructedBuilder(),
    BerSet {
    override fun encode(): ByteArray {
        // First encode children to get their content
        val childrenBytes = children.flatMap { it.encode().toList() }

        // Create new element with SET tag and children as content
        return BerInternalUtils.encodeBerElement(
            BerTagClass.UNIVERSAL,
            BerTag.SET,
            childrenBytes.toByteArray(),
            useUnboundedLength,
        )
    }

    override operator fun BerElement.unaryPlus() {
        children.add(this)
    }
}

/** SET OF type builder (sorted set) */
internal class BerSetOfBuilder :
    BerConstructedBuilder(),
    BerSetOf {
    override fun encode(): ByteArray {
        // Encode children
        val encodedChildren = children.map { it.encode() }

        // Sort using a proper ByteArray comparator
        val sortedEncodedChildren =
            encodedChildren.sortedWith { a, b ->
                // Compare byte by byte
                val minLength = minOf(a.size, b.size)
                for (i in 0 until minLength) {
                    val byteA = a[i].toInt() and 0xFF
                    val byteB = b[i].toInt() and 0xFF
                    val diff = byteA - byteB
                    if (diff != 0) return@sortedWith diff
                }
                // If we get here, compare lengths
                a.size - b.size
            }

        val childrenBytes = sortedEncodedChildren.flatMap { it.toList() }

        // Create new element with SET tag and sorted children as content
        return BerInternalUtils.encodeBerElement(
            BerTagClass.UNIVERSAL,
            BerTag.SET,
            childrenBytes.toByteArray(),
            useUnboundedLength,
        )
    }

    override operator fun BerElement.unaryPlus() {
        children.add(this)
    }
}
