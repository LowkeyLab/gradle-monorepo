/** Contains all tagging-related functions and constants for the BER DSL. */
package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerElement
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass
import io.github.tacascer.kotlinx.serialization.ber.BerTaggedElement
import io.github.tacascer.kotlinx.serialization.ber.BerTaggingInfo

/** Flag to indicate a tag should be primitive (not constructed) */
const val PRIMITIVE = 0

/** Associates a tag class with a tag number */
infix fun ULong.withClass(tagClass: BerTagClass): BerTaggingInfo = BerTaggingInfo(this.toLong(), tagClass)

/** Makes a tag primitive (not constructed) */
infix fun ULong.without(flag: Int): BerTaggingInfo {
    require(flag == PRIMITIVE) { "Only PRIMITIVE flag is supported" }
    return BerTaggingInfo(this.toLong(), BerTagClass.CONTEXT_SPECIFIC, constructed = false)
}

/** Applies an implicit tag to an element */
infix fun BerElement.withImplicitTag(tag: BerTaggingInfo): BerTaggedElement {
    val berBuilder = this as BerBuilder

    // Determine if this element should be constructed based on its type
    // For implicit tagging, use the original element's constructed nature
    val isPrimitive = berBuilder is BerPrimitiveBuilder
    val constructed =
        if (!tag.constructed) {
            // If explicitly set to not constructed, respect that
            false
        } else {
            // Otherwise use the element type to determine if it's constructed
            !isPrimitive
        }

    return BerImplicitlyTaggedBuilder(berBuilder, tag.tagNumber, tag.tagClass, constructed)
}

/** Applies an explicit tag to an element */
infix fun BerElement.withExplicitTag(tag: BerTaggingInfo): BerTaggedElement =
    BerExplicitlyTaggedBuilder(this as BerBuilder, tag.tagNumber, tag.tagClass)
