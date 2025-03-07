package io.github.lowkeylab.ber.builder

import io.github.lowkeylab.ber.BerTag
import io.github.lowkeylab.ber.BerTagClass

internal object BerInternalUtils {
    /** Encodes a BER element with the specified tag and content */
    fun encodeBerElement(
        tagClass: BerTagClass,
        tag: BerTag,
        content: ByteArray,
        useUnboundedLength: Boolean = false,
    ): ByteArray {
        val result = mutableListOf<Byte>()

        // Add tag byte - constructed bit required for unbounded length
        val isConstructed = tag == BerTag.SEQUENCE || tag == BerTag.SET || useUnboundedLength
        val tagByte = ((tagClass.value shl 6) or (if (isConstructed) 0x20 else 0) or tag.value).toByte()
        result.add(tagByte)

        // Add length and content
        addLengthAndContent(result, content, useUnboundedLength)

        return result.toByteArray()
    }

    /** Encodes a BER element with a custom tag and content */
    fun encodeBerElement(
        tagClass: BerTagClass,
        tagNumber: Long,
        constructed: Boolean,
        content: ByteArray,
        useUnboundedLength: Boolean = false,
    ): ByteArray {
        val result = mutableListOf<Byte>()

        // Add tag byte - element must be constructed for unbounded length format
        val isConstructed = constructed || useUnboundedLength
        val tagByte =
            (tagClass.value shl 6) or
                (if (isConstructed) 0x20 else 0) or
                (if (tagNumber < 31) tagNumber.toInt() else 31)
        result.add(tagByte.toByte())

        // Handle multi-byte tags
        if (tagNumber >= 31) {
            encodeExtendedTagNumber(result, tagNumber)
        }

        // Add length and content
        addLengthAndContent(result, content, useUnboundedLength)

        return result.toByteArray()
    }

    /** Adds length bytes and content to the result */
    private fun addLengthAndContent(
        result: MutableList<Byte>,
        content: ByteArray,
        useUnboundedLength: Boolean,
    ) {
        if (useUnboundedLength) {
            // Use indefinite length format (0x80)
            result.add(0x80.toByte())
            // Add content
            result.addAll(content.toList())
            // Add end-of-contents marker
            result.add(0x00)
            result.add(0x00)
        } else {
            // Use definite length format
            if (content.size < 128) {
                result.add(content.size.toByte())
            } else {
                val lengthBytes = encodeLengthBytes(content.size)
                result.add((lengthBytes.size or 0x80).toByte())
                result.addAll(lengthBytes.toList())
            }
            // Add content
            result.addAll(content.toList())
        }
    }

    /** Encodes extended tag number (tag >= 31) using base-128 format */
    private fun encodeExtendedTagNumber(
        result: MutableList<Byte>,
        tagNumber: Long,
    ) {
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

    /** Encodes a length value into BER length bytes */
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
