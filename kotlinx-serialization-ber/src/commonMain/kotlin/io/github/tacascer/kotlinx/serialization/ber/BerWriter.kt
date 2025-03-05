package io.github.tacascer.kotlinx.serialization.ber

/** Writer for BER format that handles low-level encoding. */
class BerWriter {
    private val buffer = mutableListOf<Byte>()

    fun writeBoolean(value: Boolean) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.BOOLEAN)
        writeLength(1)
        buffer.add(if (value) 0xFF.toByte() else 0x00)
    }

    fun writeByte(value: Byte) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.INTEGER)
        writeLength(1)
        buffer.add(value)
    }

    fun writeShort(value: Short) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.INTEGER)
        val bytes = encodeInteger(value.toInt())
        writeLength(bytes.size)
        buffer.addAll(bytes.toList())
    }

    fun writeInt(value: Int) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.INTEGER)
        val bytes = encodeInteger(value)
        writeLength(bytes.size)
        buffer.addAll(bytes.toList())
    }

    fun writeLong(value: Long) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.INTEGER)
        val bytes = encodeLong(value)
        writeLength(bytes.size)
        buffer.addAll(bytes.toList())
    }

    fun writeFloat(value: Float) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.REAL)
        // Implementation for real encoding goes here
        // Placeholder implementation
        val bytes = ByteArray(4) { 0 }
        val intBits = value.toBits()
        for (i in 0..3) {
            bytes[3 - i] = ((intBits shr (i * 8)) and 0xFF).toByte()
        }
        writeLength(bytes.size)
        buffer.addAll(bytes.toList())
    }

    fun writeDouble(value: Double) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.REAL)
        // Implementation for double encoding goes here
        // Placeholder implementation
        val bytes = ByteArray(8) { 0 }
        val longBits = value.toBits()
        for (i in 0..7) {
            bytes[7 - i] = ((longBits shr (i * 8)) and 0xFF).toByte()
        }
        writeLength(bytes.size)
        buffer.addAll(bytes.toList())
    }

    fun writeChar(value: Char) {
        writeString(value.toString())
    }

    fun writeString(value: String) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.UTF8_STRING)
        val bytes = value.encodeToByteArray()
        writeLength(bytes.size)
        buffer.addAll(bytes.toList())
    }

    fun beginStructure(name: String) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.SEQUENCE)
        // Length will be written later
        buffer.add(0x80.toByte()) // Indefinite length form
    }

    fun beginCollection(name: String, size: Int) {
        writeTag(BerTagClass.UNIVERSAL, BerTag.SEQUENCE)
        // Length will be written later
        buffer.add(0x80.toByte()) // Indefinite length form
    }

    fun endStructure(name: String) {
        // End-of-contents octets
        buffer.add(0x00)
        buffer.add(0x00)
    }

    private fun writeTag(tagClass: BerTagClass, tag: BerTag) {
        val tagByte = (tagClass.value shl 6) or (if (tag.value < 31) tag.value else 31)
        buffer.add(tagByte.toByte())

        // Handle multi-byte tags
        if (tag.value >= 31) {
            var value = tag.value
            val bytes = mutableListOf<Byte>()
            do {
                var b = (value and 0x7F).toByte()
                value = value shr 7
                if (bytes.isNotEmpty()) {
                    b = (b.toInt() or 0x80).toByte()
                }
                bytes.add(0, b)
            } while (value > 0)
            buffer.addAll(bytes)
        }
    }

    private fun writeLength(length: Int) {
        if (length < 128) {
            buffer.add(length.toByte())
        } else {
            val bytes = encodeInteger(length)
            buffer.add((0x80 or bytes.size).toByte())
            buffer.addAll(bytes.toList())
        }
    }

    private fun encodeInteger(value: Int): ByteArray {
        var temp = value
        var size = 1
        var val2 = value shr 8
        while (val2 != 0 && val2 != -1) {
            size++
            val2 = val2 shr 8
        }

        // Check if an extra byte is needed to distinguish negative numbers
        if ((value > 0 && (value shr (8 * size - 1) and 1) != 0) ||
                        (value < 0 && (value shr (8 * size - 1) and 1) != 1)
        ) {
            size++
        }

        val bytes = ByteArray(size)
        for (i in size - 1 downTo 0) {
            bytes[size - i - 1] = ((temp shr (i * 8)) and 0xFF).toByte()
        }
        return bytes
    }

    private fun encodeLong(value: Long): ByteArray {
        var temp = value
        var size = 1
        var val2 = value shr 8
        while (val2 != 0L && val2 != -1L) {
            size++
            val2 = val2 shr 8
        }

        // Check if an extra byte is needed to distinguish negative numbers
        if ((value > 0 && (value shr (8 * size - 1) and 1) != 0L) ||
                        (value < 0 && (value shr (8 * size - 1) and 1) != 1L)
        ) {
            size++
        }

        val bytes = ByteArray(size)
        for (i in size - 1 downTo 0) {
            bytes[size - i - 1] = ((temp shr (i * 8)) and 0xFF).toByte()
        }
        return bytes
    }

    fun toByteArray(): ByteArray = buffer.toByteArray()
}
