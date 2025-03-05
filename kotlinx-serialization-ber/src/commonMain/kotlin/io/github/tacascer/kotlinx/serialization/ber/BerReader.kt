package io.github.tacascer.kotlinx.serialization.ber

/** Reader for BER format that handles low-level decoding. */
class BerReader(private val bytes: ByteArray) {
    private var position = 0
    private var currentElementIndex = 0

    fun readBoolean(): Boolean {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.BOOLEAN) {
            "Expected BOOLEAN tag"
        }
        val length = readLength()
        require(length == 1) { "Invalid length for BOOLEAN: $length" }
        val value = bytes[position++]
        return value != 0.toByte()
    }

    fun readByte(): Byte {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.INTEGER) {
            "Expected INTEGER tag"
        }
        val length = readLength()
        require(length == 1) { "Invalid length for Byte: $length" }
        return bytes[position++]
    }

    fun readShort(): Short {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.INTEGER) {
            "Expected INTEGER tag"
        }
        val length = readLength()
        val result = decodeInteger(length)
        require(result in Short.MIN_VALUE..Short.MAX_VALUE) {
            "Value out of range for Short: $result"
        }
        return result.toShort()
    }

    fun readInt(): Int {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.INTEGER) {
            "Expected INTEGER tag"
        }
        val length = readLength()
        return decodeInteger(length)
    }

    fun readLong(): Long {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.INTEGER) {
            "Expected INTEGER tag"
        }
        val length = readLength()
        return decodeLong(length)
    }

    fun readFloat(): Float {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.REAL) { "Expected REAL tag" }
        val length = readLength()
        // Basic implementation - not full ASN.1 REAL encoding
        require(length == 4) { "Invalid length for Float: $length" }
        var bits = 0
        for (i in 0 until 4) {
            bits = (bits shl 8) or (bytes[position++].toInt() and 0xFF)
        }
        return Float.fromBits(bits)
    }

    fun readDouble(): Double {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.REAL) { "Expected REAL tag" }
        val length = readLength()
        // Basic implementation - not full ASN.1 REAL encoding
        require(length == 8) { "Invalid length for Double: $length" }
        var bits = 0L
        for (i in 0 until 8) {
            bits = (bits shl 8) or (bytes[position++].toLong() and 0xFF)
        }
        return Double.fromBits(bits)
    }

    fun readChar(): Char {
        val str = readString()
        require(str.length == 1) { "Expected single character" }
        return str[0]
    }

    fun readString(): String {
        val (tagClass, tag) = readTag()
        require(
                tagClass == BerTagClass.UNIVERSAL &&
                        (tag == BerTag.UTF8_STRING ||
                                tag == BerTag.PRINTABLE_STRING ||
                                tag == BerTag.IA5_STRING)
        ) { "Expected string tag" }

        val length = readLength()
        val stringBytes = bytes.copyOfRange(position, position + length)
        position += length
        return stringBytes.decodeToString()
    }

    fun beginStructure(name: String) {
        val (tagClass, tag) = readTag()
        require(tagClass == BerTagClass.UNIVERSAL && tag == BerTag.SEQUENCE) {
            "Expected SEQUENCE tag"
        }

        // Check for indefinite length
        val firstLengthByte = bytes[position++].toInt() and 0xFF
        require(firstLengthByte == 0x80) { "Expected indefinite length encoding" }

        // Reset element index
        currentElementIndex = 0
    }

    fun endStructure(name: String) {
        // Expecting end-of-contents marker
        val b1 = bytes[position++]
        val b2 = bytes[position++]
        require(b1 == 0.toByte() && b2 == 0.toByte()) { "Expected end-of-contents octets" }
    }

    fun decodeElementIndex(elementsCount: Int): Int {
        // Check if we've reached the end-of-contents marker
        if (position + 1 < bytes.size &&
                        bytes[position] == 0.toByte() &&
                        bytes[position + 1] == 0.toByte()
        ) {
            return -1 // End of structure
        }

        if (currentElementIndex >= elementsCount) {
            return -1 // All elements read
        }

        return currentElementIndex++
    }

    private fun readTag(): Pair<BerTagClass, BerTag> {
        val b = bytes[position++].toInt() and 0xFF
        val tagClass = BerTagClass.fromValue(b shr 6)

        // Get the tag number
        var tagNumber = b and 0x1F

        // Handle multi-byte tags
        if (tagNumber == 31) {
            tagNumber = 0
            var b2: Int
            do {
                b2 = bytes[position++].toInt() and 0xFF
                tagNumber = (tagNumber shl 7) or (b2 and 0x7F)
            } while (b2 and 0x80 != 0)
        }

        return Pair(tagClass, BerTag.fromValue(tagNumber))
    }

    private fun readLength(): Int {
        val b = bytes[position++].toInt() and 0xFF

        // Short form
        if (b and 0x80 == 0) {
            return b
        }

        // Long form
        val numLengthOctets = b and 0x7F
        var length = 0
        for (i in 0 until numLengthOctets) {
            length = (length shl 8) or (bytes[position++].toInt() and 0xFF)
        }
        return length
    }

    private fun decodeInteger(length: Int): Int {
        if (length == 0) return 0

        // Handle sign extension
        var result = if ((bytes[position].toInt() and 0x80) != 0) -1 else 0

        for (i in 0 until length) {
            result = (result shl 8) or (bytes[position++].toInt() and 0xFF)
        }

        return result
    }

    private fun decodeLong(length: Int): Long {
        if (length == 0) return 0

        // Handle sign extension
        var result = if ((bytes[position].toInt() and 0x80) != 0) -1L else 0L

        for (i in 0 until length) {
            result = (result shl 8) or (bytes[position++].toLong() and 0xFF)
        }

        return result
    }
}
