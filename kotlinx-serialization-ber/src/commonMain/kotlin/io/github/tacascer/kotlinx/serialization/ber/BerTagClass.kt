package io.github.tacascer.kotlinx.serialization.ber

/** BER tag classes as defined in ITU-T X.690. */
enum class BerTagClass(
    val value: Int,
) {
    UNIVERSAL(0),
    APPLICATION(1),
    CONTEXT_SPECIFIC(2),
    PRIVATE(3),
    ;

    companion object {
        fun fromValue(value: Int): BerTagClass =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown tag class: $value")
    }
}

/** Universal tags for BER encoding as defined in ITU-T X.690. */
enum class BerTag(
    val value: Int,
) {
    EOC(0),
    BOOLEAN(1),
    INTEGER(2),
    BIT_STRING(3),
    OCTET_STRING(4),
    NULL(5),
    OBJECT_IDENTIFIER(6),
    OBJECT_DESCRIPTOR(7),
    EXTERNAL(8),
    REAL(9),
    ENUMERATED(10),
    EMBEDDED_PDV(11),
    UTF8_STRING(12),
    RELATIVE_OID(13),
    SEQUENCE(16),
    SET(17),
    NUMERIC_STRING(18),
    PRINTABLE_STRING(19),
    T61_STRING(20),
    VIDEOTEX_STRING(21),
    IA5_STRING(22),
    UTC_TIME(23),
    GENERALIZED_TIME(24),
    GRAPHIC_STRING(25),
    VISIBLE_STRING(26),
    GENERAL_STRING(27),
    UNIVERSAL_STRING(28),
    CHARACTER_STRING(29),
    BMP_STRING(30),
    ;

    companion object {
        fun fromValue(value: Int): BerTag =
            values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown tag: $value")
    }
}
