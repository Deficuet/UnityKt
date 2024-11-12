package io.github.deficuet.unitykt.utils

import io.github.deficuet.unitykt.OffsetMode
import io.github.deficuet.unitykt.ReaderConfig
import java.nio.ByteOrder

class EndianByteArrayReader(
    private val array: ByteArray,
    endian: ByteOrder = ByteOrder.BIG_ENDIAN,
    override val baseOffset: Long = 0,
    config: ReaderConfig = ReaderConfig.default
): EndianDataInput(endian) {
    constructor(
        endian: ByteOrder = ByteOrder.BIG_ENDIAN,
        baseOffset: Long = 0,
        config: ReaderConfig = ReaderConfig.default,
        arrayBuilder: () -> ByteArray
    ): this(arrayBuilder(), endian, baseOffset, config)

    private val stream = when (config.offsetMode) {
        OffsetMode.MANUAL -> ByteArrayReader(array, config.manualOffset.toInt())
        else -> ByteArrayReader(array)
    }

    override val ignoredOffset = when (config.offsetMode) {
        OffsetMode.MANUAL -> config.manualOffset
        else -> {
            var b: Int
            do {
                b = read()
                if (b == -1) throw IllegalStateException("Input byte[] is Empty")
            } while (b == 0)
            stream.seek(stream.tell() - 1)
            stream.tell().toLong()
        }
    }

    override var position
        get() = stream.tell() - ignoredOffset
        set(value) { stream.seek((value + ignoredOffset).toInt()) }

    override val length
        get() = array.size.toLong() - ignoredOffset

    override val bytes: ByteArray
        get() = withMark {
            position = 0
            read((length - position).toInt())
        }

    override fun read() = stream.read()

    override fun read(buf: ByteArray) = stream.read(buf)

    override fun read(size: Int): ByteArray {
        if (size <= 0 || position >= length) {
            return byteArrayOf()
        }
        return ByteArray(size).apply {
            read(this)
        }
    }

    override fun close() {
        stream.close()
    }
}