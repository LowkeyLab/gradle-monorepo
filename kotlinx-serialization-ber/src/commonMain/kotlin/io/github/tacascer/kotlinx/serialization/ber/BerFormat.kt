package io.github.tacascer.kotlinx.serialization.ber

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * BER (Basic Encoding Rules) format implementation for kotlinx.serialization.
 *
 * BER is a format used in ASN.1 to encode data structures.
 */
class BerFormat(override val serializersModule: SerializersModule = EmptySerializersModule()) :
        BinaryFormat {

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val writer = BerWriter()
        val encoder = BerEncoder(writer, serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return writer.toByteArray()
    }

    override fun <T> decodeFromByteArray(
            deserializer: DeserializationStrategy<T>,
            bytes: ByteArray
    ): T {
        val reader = BerReader(bytes)
        val decoder = BerDecoder(reader, serializersModule)
        return decoder.decodeSerializableValue(deserializer)
    }
}
