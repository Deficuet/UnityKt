package io.github.deficuet.unitykt.export

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class EndianByteArrayWriter(
    size: Int,
    var endianType: ByteOrder = ByteOrder.BIG_ENDIAN
): AutoCloseable {
    val array = ByteArray(size)
    val length = size
    var position = 0

    @Suppress("SameParameterValue")
    private fun newByteBuffer(capacity: Int) = ByteBuffer.allocate(capacity).order(endianType)

    private fun write(bytes: ByteArray) {
        System.arraycopy(bytes, 0, array, position, bytes.size)
        position += bytes.size
    }

    fun writeInt(value: Int) = write(newByteBuffer(4).putInt(value).array())
    fun writeUInt(value: UInt) = writeInt(value.toInt())

    override fun close() {  }
}