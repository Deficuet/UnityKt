package io.github.deficuet.unitykt.util

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.io.FileInputStream
import io.github.deficuet.unitykt.math.*

enum class EndianType {
    LittleEndian, BigEndian
}

enum class OffsetMode {
    /**
     * Stream will apply the extra offset directly starting from position 0.
     */
    MANUAL,

    /**
     * Stream will seek automatically to the first byte that is not 0. Then apply the extra offset.
     */
    AUTO
}

sealed class EndianBinaryReader: AutoCloseable {
//    val realOffset: Long get() = baseOffset + position
//    abstract val bytes: ByteArray
    /**
     * `getter` - Absolute position
     *
     * `setter` - Relative position
     */
    abstract var position: Long

    protected abstract val endian: EndianType
    protected abstract val offsetMode: OffsetMode
    protected abstract val manualOffset: Long
    protected abstract val length: Long
    protected abstract val baseOffset: Long     //Should be last initialized

    protected fun initOffset(): Long {
        return if (offsetMode == OffsetMode.MANUAL) {
            manualOffset
        } else {
            var first: Byte
            do {
                val array = read(1)
                if (array.isEmpty()) throw IllegalStateException("Stream is Empty")
                else first = array[0]
            } while (first == 0.toByte())
            position -= 1
            position + manualOffset
        }
    }

    abstract fun read(size: Int): ByteArray

    fun readByte(): Byte = ByteBuffer.wrap(read(1)).get()
    fun readUByte(): UByte = readByte().toUByte()
    fun readShort(): Short = ByteBuffer.wrap(read(2).rearrange(endian)).short
    fun readUShort(): UShort = readShort().toUShort()
    fun readInt(): Int = ByteBuffer.wrap(read(4).rearrange(endian)).int
    fun readUInt(): UInt = readInt().toUInt()
    fun readLong(): Long = ByteBuffer.wrap(read(8).rearrange(endian)).long
    fun readULong(): ULong = readLong().toULong()
    fun readFloat(): Float = ByteBuffer.wrap(read(4).rearrange(endian)).float
    fun readDouble(): Double = ByteBuffer.wrap(read(8).rearrange(endian)).double
    fun readBool(): Boolean = readByte() != 0.toByte()
    fun readString(size: Int = -1, encode: Charset = Charsets.UTF_8): String {
        return if (size == -1) readStringToNull(charset = encode) else read(size).decodeToString(encode)
    }
    fun readStringToNull(maxLength: Int = 32767, charset: Charset = Charsets.UTF_8): String {
        val ret = mutableListOf<Byte>()
        var c: Byte? = null
        while (c != 0.toByte() && ret.size < maxLength && position != length) {
            if (c != null) ret.add(c)
            val nextByte = read(1)
            if (nextByte.isEmpty()) throw IllegalStateException("Unterminated String: $ret")
            c = nextByte[0]
        }
        return ret.toByteArray().decodeToString(charset)
    }
    fun readAlignedString(encode: Charset = Charsets.UTF_8): String {
        val size = readInt()
        if (size in 1..(length - position)) {
            val result = read(size).decodeToString(encode)
            alignStream()
            return result
        }
        return ""
    }
    private fun alignStream(alignment: Int = 4) {
        position += (alignment - position % alignment) % alignment - baseOffset
    }
    fun readNextByteArray(): ByteArray = read(readInt())
    private fun <R> readArray(frequency: Int, lambda: () -> R): List<R> {
        return mutableListOf<R>().apply {
            for (i in 1..frequency) {
                add(lambda())
            }
        }
    }
    fun readNextBoolArray(): List<Boolean> = readArray(readInt(), this::readBool)
    fun readNextUShortArray(): List<UShort> = readArray(readInt(), this::readUShort)
    fun readNextIntArray(frequency: Int = 0): List<Int> =
        readArray(if (frequency == 0) readInt() else frequency, this::readInt)
    fun readNextUIntArray(frequency: Int = 0): List<UInt> =
        readArray(if (frequency == 0) readInt() else frequency, this::readUInt)
    fun readNestedUIntArray(frequency: Int = 0): List<List<UInt>> =
        readArray(if (frequency == 0) readInt() else frequency, this::readNextUIntArray)
    fun readNextFloatArray(frequency: Int): List<Float> =
        readArray(if (frequency == 0) readInt() else frequency, this::readFloat)
    fun readNextStringArray(): List<String> = readArray(readInt(), this::readAlignedString)
    fun readQuaternion(): Quaternion = Quaternion(readFloat(), readFloat(), readFloat(), readFloat())
    fun readMatrix4x4(): Matrix4x4 = Matrix4x4(readNextFloatArray(16))
    fun readVector2(): Vector2 = Vector2(readFloat(), readFloat())
    fun readVector3(): Vector3 = Vector3(readFloat(), readFloat(), readFloat())
    fun readVector4(): Vector4 = Vector4(readFloat(), readFloat(), readFloat(), readFloat())
    fun readColor4(): Color = Color(readFloat(), readFloat(), readFloat(), readFloat())
    fun readNextMatrixArray(): List<Matrix4x4> = readArray(readInt(), this::readMatrix4x4)
    fun readNextVector2Array(): List<Vector2> = readArray(readInt(), this::readVector2)
    fun readNextVector4Array(): List<Vector4> = readArray(readInt(), this::readVector4)
}

class EndianByteArrayReader(
    private val array: ByteArray,
    override val endian: EndianType = EndianType.BigEndian,
    override val manualOffset: Long = 0,
    override val offsetMode: OffsetMode = OffsetMode.AUTO
): EndianBinaryReader() {
    override val length = array.size.toLong()
    override var position = 0L
        set(value) { field = value + baseOffset }

    override val baseOffset = initOffset()

    override fun read(size: Int): ByteArray {
        if (size <= 0 || position >= length) {
            return byteArrayOf()
        }
        val positionInt = position.toInt()
        val ret = array.sliceArray(positionInt until positionInt + size)
        position += size - baseOffset   //absolute position
        return ret
    }

    override fun close() {  }
}

class EndianFileStreamReader(
    filePath: String,
    override val endian: EndianType = EndianType.BigEndian,
    override val manualOffset: Long = 0,
    override val offsetMode: OffsetMode = OffsetMode.AUTO
): EndianBinaryReader() {
    init {
        if (!Files.isRegularFile(Path.of(filePath)))
            throw IllegalStateException("Path $filePath must be a file.")
    }
    private val stream = FileInputStream(filePath)
    private val channel get() = stream.channel
    override val length = channel.size()
    override var position: Long
        get() = channel.position()
        set(value) { channel.position(value + baseOffset) }

    override val baseOffset = initOffset()

    override fun read(size: Int): ByteArray {
        if (size <= 0 || position >= length) {
            return byteArrayOf()
        }
        val b = ByteArray(size)
        stream.read(b)
        return b
    }

    override fun close() {
        stream.close()
    }
}
