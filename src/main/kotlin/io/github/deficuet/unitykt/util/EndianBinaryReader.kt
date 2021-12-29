package io.github.deficuet.unitykt.util

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.io.FileInputStream
import java.io.Closeable
import io.github.deficuet.unitykt.file.ClassIDType
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.math.*

enum class EndianType {
    LittleEndian, BigEndian
}

enum class FileType {
    ASSETS, BUNDLE, WEB, RESOURCE   //, GZIP, BROTLI
}

enum class OffsetMode {
    MANUAL,
    /**
     * Stream will seek automatically to the first non-zero byte.
     */
    AUTO
}

sealed class EndianBinaryReader(private val manualIgnoredOffset: Long): Closeable {
    abstract val bytes: ByteArray
    /**
     * `getter` - Absolute position
     *
     * `setter` - Relative position
     */
    abstract var position: Long
    abstract val length: Long
    abstract val baseOffset: Long
    abstract var endian: EndianType
        protected set
    abstract val ignoredOffset: Long
    protected abstract val offsetMode: OffsetMode

    val fileType: FileType by lazy {
        if (length < 20) return@lazy FileType.RESOURCE
        when (runThenReset { readStringUntilNull(20) }) {
            "UnityWeb", "UnityRaw", "UnityArchive", "UnityFS" -> return@lazy FileType.BUNDLE
            "UnityWebData1.0" -> return@lazy FileType.WEB
            else -> {
                var magic = runThenReset { read(2) }
                if (CompressUtils.GZIP_MAGIC.contentEquals(magic)) {
                    return@lazy FileType.WEB
                }
                magic = with(this) {
                    position = 0x20
                    runThenReset { read(6) }
                }
                if (CompressUtils.BROTLI_MAGIC.contentEquals(magic)) {
                    return@lazy FileType.WEB
                }
                return@lazy if (isSerializedFile()) FileType.ASSETS else FileType.RESOURCE
            }
        }
    }

    /**
     * Mark of relative position
     */
    var mark: Long = 0
        private set
    val realOffset get() = position + baseOffset
    open val relativePosition get() = position - ignoredOffset

    protected fun initOffset(): Long {
        return if (offsetMode == OffsetMode.MANUAL) {
            manualIgnoredOffset
        } else {
            var first: Byte
            do {
                val array = read(1)
                if (array.isEmpty()) throw IllegalStateException("Stream is Empty")
                else first = array[0]
            } while (first == 0.toByte())
            position -= 1
            position
        }
    }

    operator fun plusAssign(inc: Int) { position += inc - ignoredOffset }

    operator fun plusAssign(inc: Long) { position += inc - ignoredOffset }

    abstract fun read(size: Int): ByteArray

    fun mark() { mark = relativePosition }

    inline fun <T> withMark(block: EndianBinaryReader.() -> T): T {
        mark()
        val result = this.block()
        position = mark
        return result
    }

    inline fun <T> runThenReset(crossinline block: EndianBinaryReader.() -> T): T {
        val result = this.block()
        position = 0
        return result
    }

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
        return if (size == -1) readStringUntilNull(charset = encode) else read(size).decodeToString(encode)
    }
    fun readStringUntilNull(maxLength: Int = 32767, charset: Charset = Charsets.UTF_8): String {
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
    fun alignStream(alignment: Int = 4) {
        plusAssign((alignment - relativePosition % alignment) % alignment)
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
    fun readNextFloatArray(frequency: Int = 0): List<Float> =
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
    fun <T> readArrayOf(constructor: EndianBinaryReader.() -> T): List<T> {
        val num = readInt()
        val list = mutableListOf<T>()
        for (i in 0 until num) {
            list.add(this.constructor())
        }
        return list
    }

    fun isSerializedFile(): Boolean {
        plusAssign(4)    //m_MetadataSize: UInt
        var mFileSize = readUInt().toLong()
        val mVersion = readUInt()
        var mDataOffset = readUInt().toLong()
        plusAssign(4)    //m_Endian(1), m_Reserved(3)
        if (mVersion > 22u) {
            if (length < 48) {
                position = 0
                return false
            }
            runThenReset {
                plusAssign(4)    //m_MetadataSize: UInt
                mFileSize = readLong()
                mDataOffset = readLong()
            }
        }
        position = 0
        return mFileSize == length && mDataOffset <= length
    }

    fun resetEndian(e: EndianType): EndianBinaryReader { endian = e; return this }

    companion object {
        fun assign(input: Any, offsetMode: OffsetMode): EndianBinaryReader {
            return when {
                input is ByteArray -> EndianByteArrayReader(input, offsetMode = offsetMode)
                input is String && input.isFile() -> EndianFileStreamReader(input, offsetMode = offsetMode)
                input is EndianBinaryReader -> input
                else -> throw UnsupportedFormatException("Invalid input for EndianBinaryReader")
            }
        }
    }
}

class EndianByteArrayReader(
    private val array: ByteArray,
    override var endian: EndianType = EndianType.BigEndian,
    override val baseOffset: Long = 0,
    override val offsetMode: OffsetMode = OffsetMode.MANUAL,
    ignoredOffset: Long = 0
): EndianBinaryReader(ignoredOffset) {
    constructor(
        endian: EndianType = EndianType.BigEndian,
        manualOffset: Long = 0,
        offsetMode: OffsetMode = OffsetMode.MANUAL,
        arrayBuilder: () -> ByteArray
    ): this(arrayBuilder(), endian, manualOffset, offsetMode)

    override val length = array.size.toLong()
    override var position = 0L
        set(value) { field = value + ignoredOffset }

    override val ignoredOffset = initOffset()
    override val bytes by lazy { with(array) { sliceArray(ignoredOffset.toInt() until size) } }

    override fun read(size: Int): ByteArray {
        if (size <= 0 || position >= length) {
            return byteArrayOf()
        }
        val positionInt = position.toInt()
        val ret = array.sliceArray(positionInt until positionInt + size)
        plusAssign(size)   //absolute position
        return ret
    }

    override fun close() {  }
}

class EndianFileStreamReader(
    filePath: String,
    override var endian: EndianType = EndianType.BigEndian,
    override val baseOffset: Long = 0,
    override val offsetMode: OffsetMode = OffsetMode.MANUAL,
    ignoredOffset: Long = 0
): EndianBinaryReader(ignoredOffset) {
    init {
        if (!Files.isRegularFile(Path.of(filePath)))
            throw IllegalStateException("Path $filePath must be a file.")
    }
    private val stream = FileInputStream(filePath)
    private val channel get() = stream.channel

    override val length = channel.size()
    override var position: Long
        get() = channel.position()
        set(value) { channel.position(value + ignoredOffset) }
    override val ignoredOffset = initOffset()
    override val bytes: ByteArray by lazy {
        withMark {
            position = 0
            read((length - position).toInt())
        }
    }

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

/**
 * Wrapper for the parent `reader`
 */
class ObjectReader(
    private val reader: EndianBinaryReader,
    val assetFile: SerializedFile,
    private val info: ObjectInfo
) : EndianBinaryReader(0) {
    val mPathID = info.mPathID
    val byteSize = info.byteSize
    val byteStart = info.byteStart
    val formatVersion = assetFile.header.version
    val unityVersion = assetFile.version
    val platform = assetFile.targetPlatform
    val type = if (ClassIDType.isDefined(info.classID)) {
        ClassIDType.values().first { it.id == info.classID }
    } else ClassIDType.UnknownType
    val serializedType = info.serializedType

    override val ignoredOffset = byteStart
    override val length = info.byteSize.toLong()

    /**
     * `getter` - relative position of parent reader
     *
     * `setter` - relative position of **this** reader
     *
     * `relativePosition` - getter for the relative position of **this** reader
     *
     * @see [relativePosition]
     */
    override var position: Long
        get() = reader.position - reader.ignoredOffset
        set(value) { reader.position = value + ignoredOffset }
//    override val relativePosition get() = position

    override val bytes: ByteArray by lazy {
        withMark {
            position = 0
            read(info.byteSize.toInt())
        }
    }
    override val baseOffset = reader.baseOffset
    override var endian = reader.endian
    override val offsetMode = OffsetMode.MANUAL

    fun <T> readObjectArrayOf(constructor: ObjectReader.() -> T): List<T> {
        val num = readInt()
        val list = mutableListOf<T>()
        for (i in 0 until num) {
            list.add(constructor())
        }
        return list
    }

    override fun read(size: Int) = reader.read(size)

    override fun close() { reader.close() }
}