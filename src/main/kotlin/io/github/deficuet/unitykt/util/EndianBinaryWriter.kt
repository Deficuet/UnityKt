package io.github.deficuet.unitykt.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

class EndianByteArrayWriter(
    val data: ByteArray,
    endian: ByteOrder = ByteOrder.BIG_ENDIAN
) {
    constructor(size: Int, endian: ByteOrder = ByteOrder.BIG_ENDIAN): this(ByteArray(size), endian)

    private val buf = ByteBuffer.wrap(data).order(endian)

    val length get() = data.size
    var position
        get() = buf.position()
        set(value) { buf.position(value) }

    var endian = endian
        set(value) {
            buf.order(value)
            field = value
        }

    fun write(b: ByteArray) { buf.put(b) }
    fun writeInt32(i: Int) { buf.putInt(i) }
    fun writeUInt32(i: UInt) { buf.putInt(i.toInt()) }
}
