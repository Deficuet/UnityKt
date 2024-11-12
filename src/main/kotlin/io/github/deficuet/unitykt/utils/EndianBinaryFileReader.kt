package io.github.deficuet.unitykt.utils

import io.github.deficuet.unitykt.OffsetMode
import io.github.deficuet.unitykt.ReaderConfig
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteOrder

class EndianBinaryFileReader(
    file: File,
    endian: ByteOrder = ByteOrder.BIG_ENDIAN,
    override val baseOffset: Long = 0,
    config: ReaderConfig = ReaderConfig.default
): EndianDataInput(endian) {
    private val stream: RandomAccessFile
    override val ignoredOffset: Long

    init {
        if (!file.isFile) {
            throw IllegalArgumentException("Path ${file.path} must be a file.")
        }
        stream = RandomAccessFile(file, "r")
        ignoredOffset = when (config.offsetMode) {
            OffsetMode.MANUAL -> {
                stream.seek(config.manualOffset)
                config.manualOffset
            }
            else -> {
                var b: Int
                do {
                    b = read()
                    if (b == -1) throw IllegalStateException("File is Empty")
                } while (b == 0)
                stream.seek(stream.filePointer - 1)
                stream.filePointer
            }
        }
    }

    override var position: Long
        get() = stream.filePointer - ignoredOffset
        set(value) { stream.seek(value + ignoredOffset) }

    override val length get() = stream.length() - ignoredOffset

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