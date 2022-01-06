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
     * Relative Position
     */
    abstract var position: Long

    /**
     * Relative Length
     */
    abstract val length: Long

    /**
     * Relative offset to its "parent" endian binary reader.
     */
    abstract val baseOffset: Long
    abstract var endian: EndianType
        protected set

    /**
     * Length of bytes that are ignored from the beginning.
     */
    abstract val ignoredOffset: Long
    protected abstract val offsetMode: OffsetMode

    val absolutePosition get() = position + ignoredOffset

    /**
     * @see [FileType]
     */
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
                return@lazy if (isSerializedFile) FileType.ASSETS else FileType.RESOURCE
            }
        }
    }

    private val isSerializedFile by lazy {
        runThenReset {
            plusAssign(4)    //m_MetadataSize: UInt
            var mFileSize = readUInt().toLong()
            val mVersion = readUInt()
            var mDataOffset = readUInt().toLong()
            plusAssign(4)    //m_Endian(1), m_Reserved(3)
            if (mVersion > 22u) {
                if (length < 48) {
                    return@runThenReset false
                }
                plusAssign(4)    //m_MetadataSize: UInt
                mFileSize = readLong()
                mDataOffset = readLong()
            }
            return@runThenReset mFileSize == length && mDataOffset <= length
        }
    }

    /**
     * Mark of relative position
     */
    var mark: Long = 0
        private set

    /**
     * Actual position relative to its "parent" endian binary reader.
     */
    val realOffset get() = position + baseOffset

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

    operator fun plusAssign(inc: Int) { position += inc }

    operator fun plusAssign(inc: Long) { position += inc }

    abstract fun read(size: Int): ByteArray

    fun mark() { mark = position }

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

    fun readSByte(): Byte = ByteBuffer.wrap(read(1)).get()      //-128~127
    fun readByte(): UByte = readSByte().toUByte()       //0~255
    fun readShort(): Short = ByteBuffer.wrap(read(2).rearrange(endian)).short
    fun readUShort(): UShort = readShort().toUShort()
    fun readInt(): Int = ByteBuffer.wrap(read(4).rearrange(endian)).int
    fun readUInt(): UInt = readInt().toUInt()
    fun readLong(): Long = ByteBuffer.wrap(read(8).rearrange(endian)).long
    fun readULong(): ULong = readLong().toULong()
    fun readFloat(): Float = ByteBuffer.wrap(read(4).rearrange(endian)).float
    fun readDouble(): Double = ByteBuffer.wrap(read(8).rearrange(endian)).double
    fun readBool(): Boolean = readSByte() != 0.toByte()
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
    open fun alignStream(alignment: Int = 4) {
        plusAssign((alignment - position % alignment) % alignment)
    }
    fun readNextByteArray(): ByteArray = read(readInt())
    private fun <R> readArray(frequency: Int, lambda: () -> R): List<R> {
        return mutableListOf<R>().apply {
            for (i in 1..frequency) {
                add(lambda())
            }
        }
    }
    private fun <R> readArrayIndexed(frequency: Int, lambda: (Int) -> R): List<R> {
        return mutableListOf<R>().apply {
            for (i in 0 until frequency) {
                add(lambda(i))
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
    fun readRectangle(): Rectangle = Rectangle(readFloat(), readFloat(), readFloat(), readFloat())
    fun readQuaternion(): Quaternion = Quaternion(readFloat(), readFloat(), readFloat(), readFloat())
    private fun readMatrix4x4(): Matrix4x4 = Matrix4x4(readNextFloatArray(16))
    fun readVector2(): Vector2 = Vector2(readFloat(), readFloat())
    fun readVector3(): Vector3 = Vector3(readFloat(), readFloat(), readFloat())
    fun readVector4(): Vector4 = Vector4(readFloat(), readFloat(), readFloat(), readFloat())
    fun readColor4(): Color = Color(readFloat(), readFloat(), readFloat(), readFloat())
    fun readNextMatrixArray(): List<Matrix4x4> = readArray(readInt(), this::readMatrix4x4)
    fun readNextVector2Array(): List<Vector2> = readArray(readInt(), this::readVector2)
    fun readNextVector4Array(): List<Vector4> = readArray(readInt(), this::readVector4)
    fun <T> readArrayOf(frequency: Int = 0, constructor: () -> T): List<T> {
        val num = if (frequency == 0) readInt() else frequency
        return readArray(num, constructor)
    }
    fun <T> readArrayIndexedOf(frequency: Int = 0, constructor: (Int) -> T): List<T> {
        val num = if (frequency == 0) readInt() else frequency
        return readArrayIndexed(num, constructor)
    }

    fun resetEndian(e: EndianType): EndianBinaryReader { endian = e; return this }
}

class EndianByteArrayReader(
    private val array: ByteArray,
    override var endian: EndianType = EndianType.BigEndian,
    override val baseOffset: Long = 0,
    override val offsetMode: OffsetMode = OffsetMode.MANUAL,
    manualIgnoredOffset: Long = 0
): EndianBinaryReader(manualIgnoredOffset) {
    constructor(
        endian: EndianType = EndianType.BigEndian,
        manualOffset: Long = 0,
        offsetMode: OffsetMode = OffsetMode.MANUAL,
        manualIgnoredOffset: Long = 0,
        arrayBuilder: () -> ByteArray
    ): this(arrayBuilder(), endian, manualOffset, offsetMode, manualIgnoredOffset)

    override var position = 0L
        get() = field - ignoredOffset
        set(value) { field = value + ignoredOffset }

    override val ignoredOffset = initOffset()
    override val length
        get() = array.size.toLong() - ignoredOffset

    override val bytes by lazy { with(array) { sliceArray(ignoredOffset.toInt() until size) } }

    override fun read(size: Int): ByteArray {
        if (size <= 0 || position >= length) {
            return byteArrayOf()
        }
        val ret = with(absolutePosition.toInt()) {
            array.sliceArray(this until this + size)
        }
        plusAssign(size)
        return ret
    }

    override fun close() {  }
}

class EndianFileStreamReader(
    filePath: String,
    override var endian: EndianType = EndianType.BigEndian,
    override val baseOffset: Long = 0,
    override val offsetMode: OffsetMode = OffsetMode.MANUAL,
    manualIgnoredOffset: Long = 0
): EndianBinaryReader(manualIgnoredOffset) {
    init {
        if (!Files.isRegularFile(Path.of(filePath)))
            throw IllegalStateException("Path $filePath must be a file.")
    }
    private val stream = FileInputStream(filePath)
    private val channel get() = stream.channel

    override var position: Long
        get() = channel.position() - ignoredOffset
        set(value) { channel.position(value + ignoredOffset) }

    override val ignoredOffset = initOffset()
    override val length get() = channel.size() - ignoredOffset
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
    val buildType = assetFile.buildType
    val platform = assetFile.targetPlatform
    val type = if (ClassIDType.isDefined(info.classID)) {
        ClassIDType.values().first { it.id == info.classID }
    } else ClassIDType.UnknownType
    val serializedType = info.serializedType

    override val ignoredOffset = byteStart
    override val length = info.byteSize.toLong()

    override var position: Long
        get() = reader.position - reader.ignoredOffset - ignoredOffset
        set(value) { reader.position = value + ignoredOffset }

    override fun alignStream(alignment: Int) {
        plusAssign((alignment - absolutePosition % alignment) % alignment)
    }
    override val bytes: ByteArray by lazy {
        withMark {
            position = 0
            read(info.byteSize.toInt())
        }
    }
    override val baseOffset = reader.baseOffset
    override var endian = reader.endian
    override val offsetMode = OffsetMode.MANUAL

    override fun read(size: Int) = reader.read(size)

    override fun close() { reader.close() }
}