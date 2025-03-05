package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import kotlinx.datetime.Instant

/** Main entry point for the BER DSL */
public object Ber {
    // Constructed types
    public fun Sequence(init: BerSequence.() -> Unit): BerElement {
        return BerSequenceBuilder().apply(init)
    }

    public fun Set(init: BerSet.() -> Unit): BerElement {
        return BerSetBuilder().apply(init)
    }

    public fun SetOf(init: BerSetOf.() -> Unit): BerElement {
        return BerSetOfBuilder().apply(init)
    }

    // Primitive types
    public fun Bool(value: Boolean): BerElement = BerBooleanBuilder(value)
    public fun Int(value: Int): BerElement = BerIntegerBuilder(value)
    public fun Int(value: Long): BerElement = BerLongIntegerBuilder(value)
    public fun Real(value: Double): BerElement = BerRealBuilder(value)
    public fun Real(value: Float): BerElement = BerRealBuilder(value.toDouble())
    public fun Null(): BerElement = BerNullBuilder()
    public fun OctetString(value: ByteArray): BerElement = BerOctetStringBuilder(value)
    public fun ObjectIdentifier(value: String): BerElement = BerObjectIdentifierBuilder(value)

    // String types
    public fun Utf8String(value: String): BerElement = BerUtf8StringBuilder(value)
    public fun PrintableString(value: String): BerElement = BerPrintableStringBuilder(value)
    public fun NumericString(value: String): BerElement = BerNumericStringBuilder(value)
    public fun IA5String(value: String): BerElement = BerIA5StringBuilder(value)
    public fun BMPString(value: String): BerElement = BerBMPStringBuilder(value)

    // Time types
    public fun UtcTime(instant: Instant): BerElement = BerUtcTimeBuilder(instant)
    public fun GeneralizedTime(instant: Instant): BerElement = BerGeneralizedTimeBuilder(instant)
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
public fun ExplicitlyTagged(tag: ULong, init: BerSequence.() -> Unit): BerTaggedElement {
    val seq = Ber.Sequence(init) as BerBuilder
    return BerExplicitlyTaggedBuilder(seq, tag.toLong(), BerTagClass.CONTEXT_SPECIFIC)
}
