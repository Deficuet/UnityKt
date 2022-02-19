package io.github.deficuet.unitykt.export

import io.github.deficuet.unitykt.util.EndianType
import java.nio.ByteBuffer

internal class EndianByteArrayWriter(
    size: Int,
    var endianType: EndianType = EndianType.BigEndian
): AutoCloseable {
    val array = ByteArray(size)
    val length = size
    var position = 0

    @Suppress("SameParameterValue")
    private fun newByteBuffer(capacity: Int) = ByteBuffer.allocate(capacity).order(endianType.order)

    private fun write(bytes: ByteArray) {
        System.arraycopy(bytes, 0, array, position, bytes.size)
        position += bytes.size
    }

    fun writeInt(value: Int) = write(newByteBuffer(4).putInt(value).array())
    fun writeUInt(value: UInt) = writeInt(value.toInt())

    override fun close() {  }
}