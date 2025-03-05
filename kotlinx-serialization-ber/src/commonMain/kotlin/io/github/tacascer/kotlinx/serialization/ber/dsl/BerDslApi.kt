package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTagClass

/** Public interface representing a BER element */
public interface BerElement {
    /** Encodes this element to a byte array */
    fun encode(): ByteArray
}

/** Public interface representing a BER sequence */
public interface BerSequence : BerElement {
    /** Adds an element to this sequence */
    operator fun BerElement.unaryPlus()
}

/** Public interface representing a BER set */
public interface BerSet : BerElement {
    /** Adds an element to this set */
    operator fun BerElement.unaryPlus()
}

/** Public interface representing a BER set of type (sorted set) */
public interface BerSetOf : BerElement {
    /** Adds an element to this set of type */
    operator fun BerElement.unaryPlus()
}

/** Public interface representing a BER tagged element */
public interface BerTaggedElement : BerElement

/** Public class for tagging operations */
public data class BerTaggingInfo(
        val tagNumber: Long,
        val tagClass: BerTagClass,
        val constructed: Boolean = true
)
