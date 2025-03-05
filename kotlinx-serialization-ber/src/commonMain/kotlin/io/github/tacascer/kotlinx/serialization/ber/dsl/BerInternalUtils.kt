package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass

/** Internal utility functions for BER encoding */
internal object BerInternalUtils {
    /** Encodes a BER element with the specified tag and content */
    fun encodeBerElement(tagClass: BerTagClass, tag: BerTag, content: ByteArray): ByteArray {
        // Create the result directly rather than using BerWriter
        val result = mutableListOf<Byte>()

        // Add tag byte
        val tagByte = ((tagClass.value shl 6) or tag.value).toByte()
        result.add(tagByte)

        // Add length bytes
        if (content.size < 128) {
            result.add(content.size.toByte())
        } else {
            val lengthBytes = encodeLengthBytes(content.size)
            result.add((lengthBytes.size or 0x80).toByte())
            result.addAll(lengthBytes.toList())
        }

        // Add content bytes
        result.addAll(content.toList())

        return result.toByteArray()
    }

    /** Encodes a BER element with a custom tag and content */
    fun encodeBerElement(
            tagClass: BerTagClass,
            tagNumber: Long,
            constructed: Boolean,
            content: ByteArray
    ): ByteArray {
        // Create the result directly rather than using BerWriter
        val result = mutableListOf<Byte>()

        // Add tag byte
        val tagByte =
                (tagClass.value shl 6) or
                        (if (constructed) 0x20 else 0) or
                        (if (tagNumber < 31) tagNumber.toInt() else 31)
        result.add(tagByte.toByte())

        // Handle multi-byte tags
        if (tagNumber >= 31) {
            var value = tagNumber
            val bytes = mutableListOf<Byte>()
            do {
                var b = (value and 0x7F).toByte()
                value = value shr 7
                if (bytes.isNotEmpty()) {
                    b = (b.toInt() or 0x80).toByte()
                }
                bytes.add(0, b)
            } while (value > 0)
            result.addAll(bytes)
        }

        // Add length bytes
        if (content.size < 128) {
            result.add(content.size.toByte())
        } else {
            val lengthBytes = encodeLengthBytes(content.size)
            result.add((lengthBytes.size or 0x80).toByte())
            result.addAll(lengthBytes.toList())
        }

        // Add content bytes
        result.addAll(content.toList())

        return result.toByteArray()
    }

    /** Encodes a length value into DER length bytes */
    private fun encodeLengthBytes(length: Int): ByteArray {
        var len = length
        val result = mutableListOf<Byte>()

        while (len > 0) {
            result.add(0, (len and 0xFF).toByte())
            len = len shr 8
        }

        return result.toByteArray()
    }
}
