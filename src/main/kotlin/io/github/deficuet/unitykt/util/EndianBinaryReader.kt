package io.github.deficuet.unitykt.util

import io.github.deficuet.unitykt.OffsetMode
import io.github.deficuet.unitykt.ReaderConfig
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.*
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

//region DataInput
sealed interface DataInput {
    fun readInt8(): Byte
    fun readUInt8(): UByte
    fun readInt16(): Short
    fun readUInt16(): UShort
    fun readInt32(): Int
    fun readUInt32(): UInt
    fun readInt64(): Long
    fun readUInt64(): ULong
    fun readFloat(): Float
    fun readDouble(): Double
    fun readBool(): Boolean
    fun readString(size: Int = -1, encoding: Charset = Charsets.UTF_8): String
    fun readNullString(maxLength: Int = 32767, charset: Charset = Charsets.UTF_8): String
    fun readAlignedString(encode: Charset = Charsets.UTF_8): String
    fun readInt8Array(): ByteArray
    fun readUInt16Array(size: Int = -1): Array<UShort>
    fun readInt32Array(size: Int = -1): IntArray
    fun readUInt32Array(size: Int = -1): Array<UInt>
    fun readNestedUInt32Array(size: Int = -1): Array<Array<UInt>>
    fun readFloatArray(size: Int = -1): FloatArray
    fun readBoolArray(size: Int = -1): BooleanArray
    fun readAlignedStringArray(size: Int = -1): Array<String>
    fun readRectangle(): Rectangle
    fun readQuaternion(): Quaternion
    fun readMatrix4x4(): Matrix4x4
    fun readVector2(): Vector2
    fun readVector3(): Vector3
    fun readVector4(): Vector4
    fun readColor4(): Color
    fun readMatrix4x4Array(size: Int = -1): Array<Matrix4x4>
    fun readVector2Array(size: Int = -1): Array<Vector2>
    fun readNestedVector2Array(size: Int = -1): Array<Array<Vector2>>
    fun readVector3Array(size: Int = -1): Array<Vector3>
    fun readVector4Array(size: Int = -1): Array<Vector4>
}
//endregion

sealed class EndianBinaryReader: Closeable, DataInput {
    abstract val bytes: ByteArray
    abstract var position: Long
    abstract val length: Long
    abstract var endian: ByteOrder

    /**
     * Relative offset to its "parent" endian binary reader.
     */
    abstract val baseOffset: Long

    /**
     * Length of bytes that are ignored from the beginning.
     */
    abstract val ignoredOffset: Long

    var absolutePosition: Long
        get() = position + ignoredOffset
        set(value) { position = value - ignoredOffset }

    /**
     * Actual position relative to its "parent" endian binary reader.
     */
    val realOffset get() = position + baseOffset

    abstract fun read(): Int
    abstract fun read(buf: ByteArray): Int
    abstract fun read(size: Int): ByteArray

    fun skip(n: Int): EndianBinaryReader {
        position += n
        return this
    }

    fun skip(n: Long): EndianBinaryReader {
        position += n
        return this
    }

    open fun alignStream(alignment: Int = 4) {
        skip((alignment - position % alignment) % alignment)
    }
}

inline fun <R: EndianBinaryReader, reified T> R.readArrayOf(
    size: Int = -1,
    crossinline constructor: R.() -> T
): Array<T> {
    val num = if (size == -1) readInt32() else size
    return Array(num) { constructor() }
}

inline fun <R: EndianBinaryReader, reified T> R.readArrayIndexedOf(
    size: Int = -1,
    crossinline constructor: R.(Int) -> T
): Array<T> {
    val num = if (size == -1) readInt32() else size
    return Array(num) { constructor(it) }
}

inline fun <R: EndianBinaryReader, T> R.withMark(crossinline block: R.() -> T): T {
    val mark = position
    val result = this.block()
    position = mark
    return result
}

inline fun <R: EndianBinaryReader, T> R.runThenReset(crossinline block: R.() -> T): T {
    val result = this.block()
    position = 0
    return result
}

inline fun <R: EndianBinaryReader, T> R.useEndian(e: ByteOrder, crossinline block: R.() -> T): T {
    val cache = endian
    endian = e
    val result = this.block()
    endian = cache
    return result
}

sealed class AbstractEndianBinaryReader(endian: ByteOrder): EndianBinaryReader() {
    private val arr2 = ByteArray(2)
    private val arr4 = ByteArray(4)
    private val arr8 = ByteArray(8)
    private val buf2 = ByteBuffer.wrap(arr2).order(endian)
    private val buf4 = ByteBuffer.wrap(arr4).order(endian)
    private val buf8 = ByteBuffer.wrap(arr8).order(endian)

    override var endian: ByteOrder = endian
        set(value) {
            buf2.order(value)
            buf4.order(value)
            buf8.order(value)
            field = value
        }

    override fun readInt8() = read().toByte()
    override fun readUInt8() = read().toUByte()
    override fun readInt16(): Short {
        read(arr2)
        return buf2.position(0).short
    }
    override fun readUInt16() = readInt16().toUShort()
    override fun readInt32(): Int {
        read(arr4)
        return buf4.position(0).int
    }
    override fun readUInt32() = readInt32().toUInt()
    override fun readInt64(): Long {
        read(arr8)
        return buf8.position(0).long
    }
    override fun readUInt64() = readInt64().toULong()
    override fun readFloat(): Float {
        read(arr4)
        return buf4.position(0).float
    }
    override fun readDouble(): Double {
        read(arr8)
        return buf8.position(0).double
    }
    override fun readBool() = read() != 0
    override fun readString(size: Int, encoding: Charset): String {
        return if (size == -1) readNullString(charset = encoding) else read(size).decodeToString(encoding)
    }
    override fun readNullString(maxLength: Int, charset: Charset): String {
        val ret = mutableListOf<Byte>()
        var b: Int
        for (i in 0 until maxLength) {
            b = read()
            if (b == 0) break
            if (b == -1) throw IllegalStateException("Unterminated String")
            ret.add(b.toByte())
        }
        return ret.toByteArray().decodeToString(charset)
    }
    override fun readAlignedString(encode: Charset): String {
        val size = readInt32()
        if (size in 1..(length - position)) {
            val result = read(size).decodeToString(encode)
            alignStream()
            return result
        }
        return ""
    }
    override fun readInt8Array() = read(readInt32())
    override fun readUInt16Array(size: Int) = readArrayOf(size) { readUInt16() }
    override fun readInt32Array(size: Int) = readArrayOf(size) { readInt32() }.toIntArray()
    override fun readUInt32Array(size: Int) = readArrayOf(size) { readUInt32() }
    override fun readNestedUInt32Array(size: Int) = readArrayOf(size) { readUInt32Array() }
    override fun readFloatArray(size: Int) = readArrayOf(size) { readFloat() }.toFloatArray()
    override fun readBoolArray(size: Int) = readArrayOf(size) { readBool() }.toBooleanArray()
    override fun readAlignedStringArray(size: Int) = readArrayOf(size) { readAlignedString() }
    override fun readRectangle() = Rectangle(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readQuaternion() = Quaternion(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readMatrix4x4() = Matrix4x4(readArrayOf(4) { readFloatArray(4) })
    override fun readVector2() = Vector2(readFloat(), readFloat())
    override fun readVector3() = Vector3(readFloat(), readFloat(), readFloat())
    override fun readVector4() = Vector4(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readColor4() = Color(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readMatrix4x4Array(size: Int) = readArrayOf(size) { readMatrix4x4() }
    override fun readVector2Array(size: Int) = readArrayOf(size) { readVector2() }
    override fun readNestedVector2Array(size: Int) = readArrayOf(size) { readVector2Array() }
    override fun readVector3Array(size: Int) = readArrayOf(size) { readVector3() }
    override fun readVector4Array(size: Int) = readArrayOf(size) { readVector4() }
}

class EndianByteArrayReader(
    private val array: ByteArray,
    endian: ByteOrder = ByteOrder.BIG_ENDIAN,
    override val baseOffset: Long = 0,
    config: ReaderConfig = ReaderConfig()
): AbstractEndianBinaryReader(endian) {
    constructor(
        endian: ByteOrder = ByteOrder.BIG_ENDIAN,
        baseOffset: Long = 0,
        config: ReaderConfig = ReaderConfig(),
        arrayBuilder: () -> ByteArray
    ): this(arrayBuilder(), endian, baseOffset, config)

    private val offsetMode: OffsetMode = config.offsetMode
    private val manualOffset: Long = config.manualOffset

    private val stream: ByteArrayReader = when (offsetMode) {
        OffsetMode.MANUAL -> ByteArrayReader(array, manualOffset.toInt())
        else -> ByteArrayReader(array)
    }

    override val ignoredOffset: Long = when (offsetMode) {
        OffsetMode.MANUAL -> manualOffset
        else -> {
            var first: Int
            do {
                first = read()
                if (first == -1) throw IllegalStateException("Input byte[] is Empty")
            } while (first == 0)
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

class EndianBinaryFileReader(
    file: File,
    endian: ByteOrder = ByteOrder.BIG_ENDIAN,
    override val baseOffset: Long = 0,
    config: ReaderConfig = ReaderConfig()
): AbstractEndianBinaryReader(endian) {
    private val offsetMode: OffsetMode = config.offsetMode
    private val manualOffset: Long = config.manualOffset
    private val stream = RandomAccessFile(file, "r")
    override val ignoredOffset: Long

    init {
        if (!file.isFile)
            throw IllegalArgumentException("Path ${file.path} must be a file.")
        ignoredOffset = when (offsetMode) {
            OffsetMode.MANUAL -> {
                stream.seek(manualOffset)
                manualOffset
            }
            else -> {
                var first: Int
                do {
                    first = read()
                    if (first == -1) throw IllegalStateException("File is Empty")
                } while (first == 0)
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

internal class ObjectReader constructor(
    private val reader: EndianBinaryReader,
    val assetFile: SerializedFile,
    internal val info: ObjectInfo
): EndianBinaryReader(), DataInput by reader {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(assetFile.reader, assetFile, info)

    override var position: Long
        get() = reader.position - info.byteStart
        set(value) { reader.position = value + info.byteStart }

    override fun read() = reader.read()
    override fun read(buf: ByteArray) = reader.read(buf)
    override fun read(size: Int) = reader.read(size)

    override fun alignStream(alignment: Int) { reader.alignStream(alignment) }

    override var endian: ByteOrder
        get() = reader.endian
        set(value) { reader.endian = value }

    val formatVersion get() = assetFile.hVersion
    val unityVersion get() = assetFile.version

    override val ignoredOffset get() = info.byteStart
    override val length = info.byteSize.toLong()

    override val bytes: ByteArray
        get() = withMark {
            position = 0
            read(info.byteSize.toInt())
        }

    override val baseOffset = reader.baseOffset

    override fun close() {  }
}
