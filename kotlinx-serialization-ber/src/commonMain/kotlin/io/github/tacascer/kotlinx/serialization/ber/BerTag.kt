package io.github.tacascer.kotlinx.serialization.ber

/** Universal tags from X.690 */
enum class BerTag(val value: Int) {
    BOOLEAN(1),
    INTEGER(2),
    BIT_STRING(3),
    OCTET_STRING(4),
    NULL(5),
    OBJECT_IDENTIFIER(6),
    // ...other tags
}
