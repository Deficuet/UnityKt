package io.github.deficuet.unitykt.internal.utils

import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.internal.metadata.UnityObjectMetadataImpl
import io.github.deficuet.unitykt.utils.DataInput
import io.github.deficuet.unitykt.utils.EndianBinaryReader
import io.github.deficuet.unitykt.utils.withMark
import java.nio.ByteOrder

internal class ObjectReader(
    private val serializedFile: SerializedFile,
    private val metadata: UnityObjectMetadataImpl
): EndianBinaryReader(), DataInput by serializedFile.reader {
    private val reader = serializedFile.reader

    override var position: Long
        get() = reader.position - metadata.byteStart
        set(value) { reader.position = value + metadata.byteStart }

    override fun read() = reader.read()
    override fun read(buf: ByteArray) = reader.read(buf)
    override fun read(size: Int) = reader.read(size)

    override fun alignStream(alignment: Int) {
        reader.alignStream(alignment)
    }

    override var endian: ByteOrder
        get() = reader.endian
        set(value) { reader.endian = value }

    override val ignoredOffset = metadata.byteStart
    override val length = metadata.byteSize.toLong()

    override val bytes: ByteArray
        get() = withMark {
            position = 0
            read(metadata.byteSize.toInt())
        }

    override val baseOffset = reader.baseOffset

    override fun close() {  }
}
