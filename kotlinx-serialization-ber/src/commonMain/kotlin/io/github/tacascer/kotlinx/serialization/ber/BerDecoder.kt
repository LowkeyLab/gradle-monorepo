package io.github.tacascer.kotlinx.serialization.ber

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

/** Decoder implementation for BER format. */
@OptIn(ExperimentalSerializationApi::class)
class BerDecoder(private val reader: BerReader, override val serializersModule: SerializersModule) :
        AbstractDecoder() {

    override fun decodeBoolean(): Boolean = reader.readBoolean()

    override fun decodeByte(): Byte = reader.readByte()

    override fun decodeShort(): Short = reader.readShort()

    override fun decodeInt(): Int = reader.readInt()

    override fun decodeLong(): Long = reader.readLong()

    override fun decodeFloat(): Float = reader.readFloat()

    override fun decodeDouble(): Double = reader.readDouble()

    override fun decodeChar(): Char = reader.readChar()

    override fun decodeString(): String = reader.readString()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        reader.beginStructure()
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        reader.endStructure()
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return reader.decodeElementIndex(descriptor.elementsCount)
    }
}
