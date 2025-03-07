package io.github.lowkeylab.ber

/**
 * ASN.1 BER/DER tag classes (X.690) The tag class is encoded in the high 2 bits of the tag byte.
 */
enum class BerTagClass(
    val value: Int,
) {
    /** Universal tag class (0b00) - standardized ASN.1 types */
    UNIVERSAL(0),

    /** Application tag class (0b01) - application-specific types */
    APPLICATION(1),

    /** Context-specific tag class (0b10) - types specific to a particular context */
    CONTEXT_SPECIFIC(2),

    /** Private tag class (0b11) - privately defined types */
    PRIVATE(3),
}
