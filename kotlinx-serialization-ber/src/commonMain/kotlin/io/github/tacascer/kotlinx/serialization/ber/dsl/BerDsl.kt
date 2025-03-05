package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import kotlinx.datetime.Instant

/** Main entry point for the BER DSL */
object Ber {
    // Constructed types
    fun sequence(init: BerSequence.() -> Unit): BerElement = BerSequenceBuilder().apply(init)

    fun set(init: BerSet.() -> Unit): BerElement = BerSetBuilder().apply(init)

    fun setOf(init: BerSetOf.() -> Unit): BerElement = BerSetOfBuilder().apply(init)

    // Primitive types
    fun bool(value: Boolean): BerElement = BerBooleanBuilder(value)

    fun int(value: Int): BerElement = BerIntegerBuilder(value)

    fun int(value: Long): BerElement = BerLongIntegerBuilder(value)

    fun real(value: Double): BerElement = BerRealBuilder(value)

    fun real(value: Float): BerElement = BerRealBuilder(value.toDouble())

    fun nullValue(): BerElement = BerNullBuilder()

    fun octetString(value: ByteArray): BerElement = BerOctetStringBuilder(value)

    fun objectIdentifier(value: String): BerElement = BerObjectIdentifierBuilder(value)

    // String types
    fun utf8String(value: String): BerElement = BerUtf8StringBuilder(value)

    fun printableString(value: String): BerElement = BerPrintableStringBuilder(value)

    fun numericString(value: String): BerElement = BerNumericStringBuilder(value)

    fun ia5String(value: String): BerElement = BerIA5StringBuilder(value)

    fun bmpString(value: String): BerElement = BerBMPStringBuilder(value)

    // Time types
    fun utcTime(instant: Instant): BerElement = BerUtcTimeBuilder(instant)

    fun generalizedTime(instant: Instant): BerElement = BerGeneralizedTimeBuilder(instant)
}

/** Base interface for all BER builders */
internal interface BerBuilder : BerElement {
    fun getTag(): BerTag

    fun getTagClass(): BerTagClass
}

/** Base class for constructed types (SEQUENCE, SET, etc.) */
internal abstract class BerConstructedBuilder : BerBuilder {
    protected val children = mutableListOf<BerBuilder>()

    /** Adds a builder as a child of this constructed type */
    operator fun BerElement.unaryPlus() {
        children.add(this as BerBuilder)
    }
}

/** Creates an explicitly tagged element (uses a constructed form with the given tag) */
fun explicitlyTagged(
    tag: ULong,
    init: BerSequence.() -> Unit,
): BerTaggedElement {
    val seq = Ber.sequence(init) as BerBuilder
    return BerExplicitlyTaggedBuilder(seq, tag.toLong(), BerTagClass.CONTEXT_SPECIFIC)
}
