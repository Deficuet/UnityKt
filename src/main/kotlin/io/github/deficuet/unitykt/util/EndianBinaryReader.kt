package io.github.deficuet.unitykt.util

import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.math.*
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.Path

enum class EndianType(val order: ByteOrder) {
    LittleEndian(ByteOrder.LITTLE_ENDIAN),
    BigEndian(ByteOrder.BIG_ENDIAN)
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

    var absolutePosition: Long
        get() = position + ignoredOffset
        set(value) { position = value - ignoredOffset }
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
     * Actual position relative to its "parent" endian binary reader.
     */
    val realOffset get() = position + baseOffset

    protected fun initOffset(): Long {
        return if (offsetMode == OffsetMode.MANUAL) {
            manualIgnoredOffset.also { position = it }
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

    inline fun <T> withMark(block: EndianBinaryReader.() -> T): T {
        val mark = position
        val result = this.block()
        position = mark
        return result
    }

    inline fun <T> runThenReset(crossinline block: EndianBinaryReader.() -> T): T {
        val result = this.block()
        position = 0
        return result
    }

    fun readSByte(): Byte = read(1)[0]      //-128~127
    fun readByte(): UByte = readSByte().toUByte()       //0~255
    fun readShort(): Short = ByteBuffer.wrap(read(2)).order(endian.order).short
    fun readUShort(): UShort = readShort().toUShort()
    fun readInt(): Int = ByteBuffer.wrap(read(4)).order(endian.order).int
    fun readUInt(): UInt = readInt().toUInt()
    fun readLong(): Long = ByteBuffer.wrap(read(8)).order(endian.order).long
    fun readULong(): ULong = readLong().toULong()
    fun readFloat(): Float = ByteBuffer.wrap(read(4)).order(endian.order).float
    fun readDouble(): Double = ByteBuffer.wrap(read(8)).order(endian.order).double
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
    inline fun <reified R> readArray(frequency: Int, lambda: () -> R): Array<R> {
        return Array(frequency) { lambda() }
    }
    inline fun <reified R> readArrayIndexed(frequency: Int, lambda: (Int) -> R): Array<R> {
        return Array(frequency) { lambda(it) }
    }
    fun readNextBoolArray(): BooleanArray = readArray(readInt(), this::readBool).toBooleanArray()
    fun readNextUShortArray(): Array<UShort> = readArray(readInt(), this::readUShort)
    fun readNextIntArray(frequency: Int = -1): IntArray =
        readArray(if (frequency == -1) readInt() else frequency, this::readInt).toIntArray()
    fun readNextUIntArray(frequency: Int = -1): Array<UInt> =
        readArray(if (frequency == -1) readInt() else frequency, this::readUInt)
    fun readNestedUIntArray(frequency: Int = -1): Array<Array<UInt>> =
        readArray(if (frequency == -1) readInt() else frequency, this::readNextUIntArray)
    fun readNextFloatArray(frequency: Int = -1): FloatArray =
        readArray(if (frequency == -1) readInt() else frequency, this::readFloat).toFloatArray()
    fun readNextStringArray(): Array<String> = readArray(readInt(), this::readAlignedString)
    fun readRectangle(): Rectangle = Rectangle(readFloat(), readFloat(), readFloat(), readFloat())
    fun readQuaternion(): Quaternion = Quaternion(readFloat(), readFloat(), readFloat(), readFloat())
    private fun readMatrix4x4(): Matrix4x4 = Matrix4x4(*readNextFloatArray(16))
    fun readVector2(): Vector2 = Vector2(readFloat(), readFloat())
    fun readVector3(): Vector3 = Vector3(readFloat(), readFloat(), readFloat())
    fun readVector4(): Vector4 = Vector4(readFloat(), readFloat(), readFloat(), readFloat())
    fun readColor4(): Color = Color(readFloat(), readFloat(), readFloat(), readFloat())
    fun readNextMatrixArray(): Array<Matrix4x4> = readArray(readInt(), this::readMatrix4x4)
    fun readNextVector2Array(): Array<Vector2> = readArray(readInt(), this::readVector2)
    fun readNextVector4Array(): Array<Vector4> = readArray(readInt(), this::readVector4)
    inline fun <reified T> readArrayOf(frequency: Int = -1, constructor: () -> T): Array<T> {
        val num = if (frequency == -1) readInt() else frequency
        return readArray(num, constructor)
    }
    inline fun <reified T> readArrayIndexedOf(frequency: Int = -1, constructor: (Int) -> T): Array<T> {
        val num = if (frequency == -1) readInt() else frequency
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
        if (!Files.isRegularFile(Path(filePath)))
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
class ObjectReader internal constructor(
    private val reader: EndianBinaryReader,
    val assetFile: SerializedFile,
    private val info: ObjectInfo
) : EndianBinaryReader(0) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(assetFile.reader, assetFile, info)

    val mPathID = info.mPathID
    val byteSize = info.byteSize
    val byteStart = info.byteStart
    val formatVersion = assetFile.header.version
    val unityVersion = assetFile.version
    val buildType = assetFile.buildType
    val platform = assetFile.targetPlatform
    val type = info.type
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

    override fun close() {  }
}