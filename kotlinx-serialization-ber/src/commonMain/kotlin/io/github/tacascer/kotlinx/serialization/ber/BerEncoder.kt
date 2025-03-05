package io.github.tacascer.kotlinx.serialization.ber

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule

/** Encoder implementation for BER format. */
@OptIn(ExperimentalSerializationApi::class)
class BerEncoder(private val writer: BerWriter, override val serializersModule: SerializersModule) :
        AbstractEncoder() {

    override fun encodeBoolean(value: Boolean) {
        writer.writeBoolean(value)
    }

    override fun encodeByte(value: Byte) {
        writer.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        writer.writeShort(value)
    }

    override fun encodeInt(value: Int) {
        writer.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        writer.writeLong(value)
    }

    override fun encodeFloat(value: Float) {
        writer.writeFloat(value)
    }

    override fun encodeDouble(value: Double) {
        writer.writeDouble(value)
    }

    override fun encodeChar(value: Char) {
        writer.writeChar(value)
    }

    override fun encodeString(value: String) {
        writer.writeString(value)
    }

    override fun beginCollection(
            descriptor: SerialDescriptor,
            collectionSize: Int
    ): CompositeEncoder {
        writer.beginCollection(descriptor.serialName, collectionSize)
        return this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        writer.endStructure(descriptor.serialName)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        writer.beginStructure(descriptor.serialName)
        return this
    }
}
