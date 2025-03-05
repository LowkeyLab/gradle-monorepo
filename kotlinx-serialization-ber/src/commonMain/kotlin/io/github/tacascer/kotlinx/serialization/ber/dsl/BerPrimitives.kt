package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass

/** Base class for primitive type builders */
internal abstract class BerPrimitiveBuilder : BerBuilder

/** BOOLEAN type builder */
internal class BerBooleanBuilder(private val value: Boolean) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // Direct encoding of boolean value for maximum control
        return byteArrayOf(
                0x01.toByte(), // BOOLEAN tag (universal class is 0 in high bits)
                0x01.toByte(), // Length 1 byte
                (if (value) 0xFF else 0x00).toByte() // Boolean value (FF for true, 00 for false)
        )
    }

    override fun getTag() = BerTag.BOOLEAN
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** INTEGER type builder (for Int values) */
internal class BerIntegerBuilder(private val value: Int) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // Direct encoding for maximum control
        val result = mutableListOf<Byte>()
        result.add(0x02) // INTEGER tag

        // Get the properly encoded content
        val content = encodeMinimalIntegerValue(value)

        // Add length
        result.add(content.size.toByte())

        // Add content
        result.addAll(content.toList())

        return result.toByteArray()
    }

    override fun getTag() = BerTag.INTEGER
    override fun getTagClass() = BerTagClass.UNIVERSAL

    private fun encodeMinimalIntegerValue(value: Int): ByteArray {
        if (value == 0) return byteArrayOf(0)

        // Convert to bytes (big-endian)
        val allBytes = ByteArray(4)
        for (i in 0..3) {
            allBytes[3 - i] = ((value shr (i * 8)) and 0xFF).toByte()
        }

        // For positive numbers: Find first non-zero byte
        // For negative numbers: Find first byte that's not 0xFF
        val startIndex =
                if (value > 0) {
                    allBytes.indexOfFirst { it.toInt() and 0xFF != 0 }
                } else {
                    allBytes.indexOfFirst { it.toInt() and 0xFF != 0xFF }
                }

        // If all bytes are the same (all 0s or all FFs), return just the last byte
        if (startIndex == -1) {
            return byteArrayOf(allBytes[3])
        }

        // Extract the minimal byte representation
        val result = allBytes.copyOfRange(startIndex, 4)

        // For positive numbers: If the high bit is set on the first byte, add a leading zero
        if (value > 0 && (result[0].toInt() and 0x80) != 0) {
            return byteArrayOf(0) + result
        }

        // For negative numbers: If the high bit is not set on the first byte, add a leading 0xFF
        if (value < 0 && (result[0].toInt() and 0x80) == 0) {
            return byteArrayOf(0xFF.toByte()) + result
        }

        return result
    }
}

/** INTEGER type builder (for Long values) */
internal class BerLongIntegerBuilder(private val value: Long) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // Direct encoding for maximum control
        val result = mutableListOf<Byte>()
        result.add(0x02) // INTEGER tag

        // Get the properly encoded content
        val content = encodeMinimalIntegerValue(value)

        // Add length
        result.add(content.size.toByte())

        // Add content
        result.addAll(content.toList())

        return result.toByteArray()
    }

    override fun getTag() = BerTag.INTEGER
    override fun getTagClass() = BerTagClass.UNIVERSAL

    private fun encodeMinimalIntegerValue(value: Long): ByteArray {
        if (value == 0L) return byteArrayOf(0)

        // Convert to bytes (big-endian)
        val allBytes = ByteArray(8)
        for (i in 0..7) {
            allBytes[7 - i] = ((value shr (i * 8)) and 0xFF).toByte()
        }

        // For positive numbers: Find first non-zero byte
        // For negative numbers: Find first byte that's not 0xFF
        val startIndex =
                if (value > 0) {
                    allBytes.indexOfFirst { it.toInt() and 0xFF != 0 }
                } else {
                    allBytes.indexOfFirst { it.toInt() and 0xFF != 0xFF }
                }

        // If all bytes are the same (all 0s or all FFs), return just the last byte
        if (startIndex == -1) {
            return byteArrayOf(allBytes[7])
        }

        // Extract the minimal byte representation
        val result = allBytes.copyOfRange(startIndex, 8)

        // For positive numbers: If the high bit is set on the first byte, add a leading zero
        if (value > 0 && (result[0].toInt() and 0x80) != 0) {
            return byteArrayOf(0) + result
        }

        // For negative numbers: If the high bit is not set on the first byte, add a leading 0xFF
        if (value < 0 && (result[0].toInt() and 0x80) == 0) {
            return byteArrayOf(0xFF.toByte()) + result
        }

        return result
    }
}

/** REAL type builder */
internal class BerRealBuilder(private val value: Double) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // Basic IEEE 754 encoding for now
        val bits = value.toBits()
        val content = ByteArray(8)
        for (i in 0 until 8) {
            content[7 - i] = ((bits shr (i * 8)) and 0xFF).toByte()
        }
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.REAL, content)
    }

    override fun getTag() = BerTag.REAL
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** NULL type builder */
internal class BerNullBuilder : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // NULL has empty content
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.NULL, ByteArray(0))
    }

    override fun getTag() = BerTag.NULL
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** OCTET STRING type builder */
internal class BerOctetStringBuilder(private val value: ByteArray) :
        BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.OCTET_STRING, value)
    }

    override fun getTag() = BerTag.OCTET_STRING
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** OBJECT IDENTIFIER type builder */
internal class BerObjectIdentifierBuilder(private val value: String) :
        BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // Convert string OID to encoded form
        val oidComponents = value.split(".").map { it.toInt() }
        val encodedOid = encodeObjectIdentifier(oidComponents)

        return BerInternalUtils.encodeBerElement(
                BerTagClass.UNIVERSAL,
                BerTag.OBJECT_IDENTIFIER,
                encodedOid
        )
    }

    override fun getTag() = BerTag.OBJECT_IDENTIFIER
    override fun getTagClass() = BerTagClass.UNIVERSAL

    private fun encodeObjectIdentifier(components: List<Int>): ByteArray {
        // ...keep existing implementation...
        val result = mutableListOf<Byte>()

        // First two components are encoded as (40 * first) + second
        if (components.size >= 2) {
            result.add(((40 * components[0]) + components[1]).toByte())
        } else {
            result.add(0)
        }

        // Encode remaining components
        for (i in 2 until components.size) {
            val encodedComponent = encodeOidComponent(components[i])
            result.addAll(encodedComponent)
        }

        return result.toByteArray()
    }

    private fun encodeOidComponent(value: Int): List<Byte> {
        // ...keep existing implementation...
        if (value < 128) {
            return listOf(value.toByte())
        }

        val result = mutableListOf<Byte>()
        var v = value

        // Calculate required bytes
        val bytes = mutableListOf<Int>()
        while (v > 0) {
            bytes.add(0, v and 0x7F)
            v = v shr 7
        }

        // Set continuation bits
        for (i in 0 until bytes.size - 1) {
            bytes[i] = bytes[i] or 0x80
        }

        // Convert to bytes
        for (b in bytes) {
            result.add(b.toByte())
        }

        return result
    }
}
