package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import kotlinx.datetime.Instant

/** Main entry point for the BER DSL */
object Ber {
    // Constructed types
    fun Sequence(init: BerSequence.() -> Unit): BerElement {
        return BerSequenceBuilder().apply(init)
    }

    fun Set(init: BerSet.() -> Unit): BerElement {
        return BerSetBuilder().apply(init)
    }

    fun SetOf(init: BerSetOf.() -> Unit): BerElement {
        return BerSetOfBuilder().apply(init)
    }

    // Primitive types
    fun Bool(value: Boolean): BerElement = BerBooleanBuilder(value)
    fun Int(value: Int): BerElement = BerIntegerBuilder(value)
    fun Int(value: Long): BerElement = BerLongIntegerBuilder(value)
    fun Real(value: Double): BerElement = BerRealBuilder(value)
    fun Real(value: Float): BerElement = BerRealBuilder(value.toDouble())
    fun Null(): BerElement = BerNullBuilder()
    fun OctetString(value: ByteArray): BerElement = BerOctetStringBuilder(value)
    fun ObjectIdentifier(value: String): BerElement = BerObjectIdentifierBuilder(value)

    // String types
    fun Utf8String(value: String): BerElement = BerUtf8StringBuilder(value)
    fun PrintableString(value: String): BerElement = BerPrintableStringBuilder(value)
    fun NumericString(value: String): BerElement = BerNumericStringBuilder(value)
    fun IA5String(value: String): BerElement = BerIA5StringBuilder(value)
    fun BMPString(value: String): BerElement = BerBMPStringBuilder(value)

    // Time types
    fun UtcTime(instant: Instant): BerElement = BerUtcTimeBuilder(instant)
    fun GeneralizedTime(instant: Instant): BerElement = BerGeneralizedTimeBuilder(instant)
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
fun ExplicitlyTagged(tag: ULong, init: BerSequence.() -> Unit): BerTaggedElement {
    val seq = Ber.Sequence(init) as BerBuilder
    return BerExplicitlyTaggedBuilder(seq, tag.toLong(), BerTagClass.CONTEXT_SPECIFIC)
}
