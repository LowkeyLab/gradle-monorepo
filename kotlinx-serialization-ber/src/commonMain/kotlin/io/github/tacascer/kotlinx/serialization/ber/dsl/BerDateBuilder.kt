package io.github.tacascer.kotlinx.serialization.ber.dsl

/**
 * DATE type builder (non-standard)
 *
 * This implements a DATE type as a custom tag (31) using extended format. The date is encoded as a
 * string in YYYYMMDD format.
 */
class BerDateBuilder(
    private val dateString: String,
) : BerPrimitiveBuilder() {
    override fun encode(): ByteArray {
        // Format date as YYYYMMDD
        val formattedDate =
            dateString
                .replace("-", "") // Remove hyphens if present
                .toByteArray()

        // Create custom tag 31 (1F 1F) with the date content
        val result = mutableListOf<Byte>()
        // Add the tag - using extended tag format for 31
        result.add(0x1F.toByte()) // First byte indicates extended tag
        result.add(0x1F.toByte()) // Second byte is the actual tag value

        // Add length
        result.add(formattedDate.size.toByte())

        // Add content
        result.addAll(formattedDate.toList())

        return result.toByteArray()
    }
}
