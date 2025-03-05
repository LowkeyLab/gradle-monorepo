package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTagClass

/** Public interface representing a BER element */
interface BerElement {
    /** Encodes this element to a byte array */
    fun encode(): ByteArray
}

/** Public interface representing a BER sequence */
interface BerSequence : BerElement {
    /** Adds an element to this sequence */
    operator fun BerElement.unaryPlus()
}

/** Public interface representing a BER set */
interface BerSet : BerElement {
    /** Adds an element to this set */
    operator fun BerElement.unaryPlus()
}

/** Public interface representing a BER set of type (sorted set) */
interface BerSetOf : BerElement {
    /** Adds an element to this set of type */
    operator fun BerElement.unaryPlus()
}

/** Public interface representing a BER tagged element */
interface BerTaggedElement : BerElement

/** Public class for tagging operations */
data class BerTaggingInfo(
        val tagNumber: Long,
        val tagClass: BerTagClass,
        val constructed: Boolean = true
)
