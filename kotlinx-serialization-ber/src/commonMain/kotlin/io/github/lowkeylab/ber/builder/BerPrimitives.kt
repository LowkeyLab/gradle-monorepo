package io.github.lowkeylab.ber.builder

import io.github.lowkeylab.ber.BerTag
import io.github.lowkeylab.ber.BerTagClass
import java.io.ByteArrayOutputStream

/** Base class for primitive type builders */
abstract class BerPrimitiveBuilder : BerBuilder

/** BOOLEAN type builder */
class BerBooleanBuilder(
    private val value: Boolean,
) : BerPrimitiveBuilder() {
    override fun encode(): ByteArray {
        // Direct encoding of boolean value for maximum control
        return byteArrayOf(
            0x01.toByte(), // BOOLEAN tag (universal class is 0 in high bits)
            0x01.toByte(), // Length 1 byte
            (if (value) 0xFF else 0x00).toByte(), // Boolean value (FF for true, 00 for false)
        )
    }
}

/** INTEGER type builder (for Int values) */
class BerIntegerBuilder(
    private val value: Int,
) : BerPrimitiveBuilder() {
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

    private fun encodeMinimalIntegerValue(value: Int): ByteArray {
        if (value == 0) return byteArrayOf(0)

        // Convert integer to bytes in big-endian order
        val bytes = mutableListOf<Byte>()
        var v = value

        // For negative numbers, we need to be careful with sign extension
        val isNegative = value < 0

        do {
            bytes.add(0, (v and 0xFF).toByte())
            v = v shr 8
        } while (v != 0 && v != -1) // Stop at 0 for positive, -1 for negative

        // For positive numbers, if the high bit of first byte is set,
        // add a leading zero to ensure it's interpreted as positive
        if (!isNegative && (bytes[0].toInt() and 0x80) != 0) {
            bytes.add(0, 0)
        }

        // For negative numbers, if the high bit of first byte is NOT set,
        // add a leading 0xFF to ensure it's interpreted as negative
        if (isNegative && (bytes[0].toInt() and 0x80) == 0) {
            bytes.add(0, 0xFF.toByte())
        }

        return bytes.toByteArray()
    }
}

/** INTEGER type builder (for Long values) */
class BerLongIntegerBuilder(
    private val value: Long,
) : BerPrimitiveBuilder() {
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

    private fun encodeMinimalIntegerValue(value: Long): ByteArray {
        if (value == 0L) return byteArrayOf(0)

        // Convert integer to bytes in big-endian order
        val bytes = mutableListOf<Byte>()
        var v = value

        // For negative numbers, we need to be careful with sign extension
        val isNegative = value < 0

        do {
            bytes.add(0, (v and 0xFF).toByte())
            v = v shr 8
        } while (v != 0L && v != -1L) // Stop at 0 for positive, -1 for negative

        // For positive numbers, if the high bit of first byte is set,
        // add a leading zero to ensure it's interpreted as positive
        if (!isNegative && (bytes[0].toInt() and 0x80) != 0) {
            bytes.add(0, 0)
        }

        // For negative numbers, if the high bit of first byte is NOT set,
        // add a leading 0xFF to ensure it's interpreted as negative
        if (isNegative && (bytes[0].toInt() and 0x80) == 0) {
            bytes.add(0, 0xFF.toByte())
        }

        return bytes.toByteArray()
    }
}

private const val BER_REAL_SIGN_BIT_POSITION_IN_BYTE = 6

private const val BER_REAL_SIGN_BIT_MASK = 1 shl BER_REAL_SIGN_BIT_POSITION_IN_BYTE

private const val BER_REAL_FIRST_BIT_MASK = 1 shl 7

private const val IEEE_754_DOUBLE_PRECISION_EXPONENT_MASK = 0x7FF

private const val IEEE_754_DOUBLE_PRECISION_EXPONENT_BIAS = 1023

private const val IEEE_754_DOUBLE_PRECISION_SIGNIFICAND_MASK = 0xFFFFFFFFFFFFFL

private const val IEEE_754_DOUBLE_PRECISION_SIGNIFICAND_BIT_LENGTH = 52

private const val IEEE_754_DOUBLE_PRECISION_BIT_LENGTH = 63

/** REAL type builder - Fixed to use proper BER encoding */
class BerRealBuilder(
    private val value: Double,
) : BerPrimitiveBuilder() {
    override fun encode(): ByteArray {
        // Handle special cases
        return when {
            value == 0.0 -> {
                // Zero is encoded with empty content
                byteArrayOf(0x09, 0x00)
            }

            value.isNaN() -> {
                // NaN (special value 0x42)
                byteArrayOf(0x09, 0x01, 0x42)
            }

            value.isInfinite() -> {
                // Infinity (positive 0x40, negative 0x41)
                if (value > 0) {
                    byteArrayOf(0x09, 0x01, 0x40) // Positive infinity
                } else {
                    byteArrayOf(0x09, 0x01, 0x41) // Negative infinity
                }
            }

            else -> {
                // Handle normal numbers using binary encoding (base 2)
                encodeNormalValue()
            }
        }
    }

    /**
     * Encodes a normal IEEE-754 double precision value using DER constraints.
     */
    private fun encodeNormalValue(): ByteArray {
        val byteArray = ByteArrayOutputStream()
        // Get the IEEE-754 representation of the double value
        val bits = value.toRawBits()

        // Extract the sign byte
        val signByte =
            (
                (bits shr (IEEE_754_DOUBLE_PRECISION_BIT_LENGTH - BER_REAL_SIGN_BIT_POSITION_IN_BYTE)).toInt()
                    and (BER_REAL_SIGN_BIT_MASK)
            ) or (BER_REAL_FIRST_BIT_MASK)

        // Extract the exponent bits and subtract the bias to get the actual exponent
        var exponent =
            ((bits shr IEEE_754_DOUBLE_PRECISION_SIGNIFICAND_BIT_LENGTH).toInt() and IEEE_754_DOUBLE_PRECISION_EXPONENT_MASK) -
                (IEEE_754_DOUBLE_PRECISION_EXPONENT_BIAS)

        // Extract the significand and add the implicit leading 1 from the IEEE 754 format
        var mantissa =
            (bits and IEEE_754_DOUBLE_PRECISION_SIGNIFICAND_MASK) or (1L shl IEEE_754_DOUBLE_PRECISION_SIGNIFICAND_BIT_LENGTH)

        // Prepare the exponent to be incremented as many as the number of times we divide by 2
        exponent -= 52
        // mantissa must be odd, so we shift right until it is
        while ((mantissa and 1L) == 0L) {
            mantissa = mantissa shr 1
            // We just divided by 2, so we need to increment the exponent
            exponent++
        }

        val exptBytes = exponent.toBigInteger().toByteArray()
        if (exptBytes.size < 3) {
            // We need to tell how many bytes we are using for the exponent
            byteArray.write(signByte or (exptBytes.size - 1))
        } else {
            // We need to tell how many bytes we are using for the exponent
            byteArray.write(signByte or 3)
            byteArray.write(exptBytes.size)
        }

        byteArray.write(exptBytes)
        byteArray.write(mantissa.toBigInteger().toByteArray())

        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.REAL, byteArray.toByteArray())
    }
}

/** NULL type builder */
class BerNullBuilder : BerPrimitiveBuilder() {
    override fun encode(): ByteArray {
        // NULL has empty content
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.NULL, ByteArray(0))
    }
}

/** OCTET STRING type builder */
class BerOctetStringBuilder(
    private val value: ByteArray,
) : BerPrimitiveBuilder() {
    override fun encode(): ByteArray = BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.OCTET_STRING, value)
}

/** OBJECT IDENTIFIER type builder */
class BerObjectIdentifierBuilder(
    private val value: String,
) : BerPrimitiveBuilder() {
    override fun encode(): ByteArray {
        // Convert string OID to encoded form
        val oidComponents = value.split(".").map { it.toInt() }
        val encodedOid = encodeObjectIdentifier(oidComponents)

        return BerInternalUtils.encodeBerElement(
            BerTagClass.UNIVERSAL,
            BerTag.OBJECT_IDENTIFIER,
            encodedOid,
        )
    }

    private fun encodeObjectIdentifier(components: List<Int>): ByteArray {
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
