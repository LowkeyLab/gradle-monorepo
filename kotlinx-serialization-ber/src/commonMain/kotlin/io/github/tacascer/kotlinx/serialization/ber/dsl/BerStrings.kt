package io.github.tacascer.kotlinx.serialization.ber.dsl

import io.github.tacascer.kotlinx.serialization.ber.BerTag
import io.github.tacascer.kotlinx.serialization.ber.BerTagClass

/** UTF8 STRING type builder */
internal class BerUtf8StringBuilder(private val value: String) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        val content = value.encodeToByteArray()
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.UTF8_STRING, content)
    }

    override fun getTag() = BerTag.UTF8_STRING
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** PRINTABLE STRING type builder */
internal class BerPrintableStringBuilder(private val value: String) :
        BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        val content = value.encodeToByteArray()
        return BerInternalUtils.encodeBerElement(
                BerTagClass.UNIVERSAL,
                BerTag.PRINTABLE_STRING,
                content
        )
    }

    override fun getTag() = BerTag.PRINTABLE_STRING
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** NUMERIC STRING type builder */
internal class BerNumericStringBuilder(private val value: String) :
        BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        val content = value.encodeToByteArray()
        return BerInternalUtils.encodeBerElement(
                BerTagClass.UNIVERSAL,
                BerTag.NUMERIC_STRING,
                content
        )
    }

    override fun getTag() = BerTag.NUMERIC_STRING
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** IA5 STRING (ASCII) type builder */
internal class BerIA5StringBuilder(private val value: String) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        val content = value.encodeToByteArray()
        return BerInternalUtils.encodeBerElement(BerTagClass.UNIVERSAL, BerTag.IA5_STRING, content)
    }

    override fun getTag() = BerTag.IA5_STRING
    override fun getTagClass() = BerTagClass.UNIVERSAL
}

/** BMP STRING (Basic Multilingual Plane - UCS-2) type builder */
internal class BerBMPStringBuilder(private val value: String) : BerPrimitiveBuilder(), BerElement {
    override fun encode(): ByteArray {
        // Convert to UCS-2 big endian
        val bytes = mutableListOf<Byte>()
        for (char in value) {
            bytes.add((char.code shr 8).toByte())
            bytes.add((char.code and 0xFF).toByte())
        }

        return BerInternalUtils.encodeBerElement(
                BerTagClass.UNIVERSAL,
                BerTag.BMP_STRING,
                bytes.toByteArray()
        )
    }

    override fun getTag() = BerTag.BMP_STRING
    override fun getTagClass() = BerTagClass.UNIVERSAL
}
