package io.github.deficuet.unitykt.utils

import io.github.deficuet.unitykt.math.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

abstract class EndianDataInput(endian: ByteOrder): EndianBinaryReader() {
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
    override fun readString(size: Int, charset: Charset): String {
        return if (size == -1) readNullString(charset = charset) else String(read(size), charset)
    }
    override fun readNullString(maxLength: Int, charset: Charset): String {
        val ret = ByteArray(maxLength)
        var b: Int; var count = 0
        while (position < length && count < maxLength) {
            b = read()
            if (b == 0) break
            ret[count++] = b.toByte()
        }
        return String(ret, 0, count, charset)
    }
    override fun readAlignedString(charset: Charset): String {
        val size = readInt32()
        if (size in 1..(length - position)) {
            val result = String(read(size), charset)
            alignStream()
            return result
        }
        return ""
    }
    override fun readInt8Array(size: Int) = read(autoSize(size))
    override fun readUInt8Array(size: Int) = readArrayOf(size) { readUInt8() }
    override fun readInt16Array(size: Int) = ShortArray(autoSize(size)) { readInt16() }
    override fun readUInt16Array(size: Int) = readArrayOf(size) { readUInt16() }
    override fun readInt32Array(size: Int) = IntArray(autoSize(size)) { readInt32() }
    override fun readUInt32Array(size: Int) = readArrayOf(size) { readUInt32() }
    override fun readNestedUInt32Array(size: Int) = readArrayOf(size) { readUInt32Array() }
    override fun readInt64Array(size: Int) = LongArray(autoSize(size)) { readInt64() }
    override fun readUInt64Array(size: Int) = readArrayOf(size) { readUInt64() }
    override fun readFloatArray(size: Int) = FloatArray(autoSize(size)) { readFloat() }
    override fun readBoolArray(size: Int) = BooleanArray(autoSize(size)) { readBool() }
    override fun readDoubleArray(size: Int) = DoubleArray(autoSize(size)) { readDouble() }
    override fun readAlignedStringArray(size: Int) = readArrayOf(size) { readAlignedString() }
    override fun readRect() = Rect(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readQuaternion() = Quaternion(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readMatrix4x4() = Matrix4x4(readArrayOf(4) { readFloatArray(4) })
    override fun readVector2() = Vector2(readFloat(), readFloat())
    override fun readVector3() = Vector3(readFloat(), readFloat(), readFloat())
    override fun readVector4() = Vector4(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readColor() = Color(readFloat(), readFloat(), readFloat(), readFloat())
    override fun readMatrix4x4Array(size: Int) = readArrayOf(size) { readMatrix4x4() }
    override fun readVector2Array(size: Int) = readArrayOf(size) { readVector2() }
    override fun readNestedVector2Array(size: Int) = readArrayOf(size) { readVector2Array() }
    override fun readVector3Array(size: Int) = readArrayOf(size) { readVector3() }
    override fun readVector4Array(size: Int) = readArrayOf(size) { readVector4() }
}
