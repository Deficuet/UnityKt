package io.github.deficuet.unitykt.utils

import io.github.deficuet.unitykt.math.*
import java.nio.charset.Charset

interface DataInput {
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
    fun readString(size: Int = -1, charset: Charset = Charsets.UTF_8): String
    fun readNullString(maxLength: Int = 32767, charset: Charset = Charsets.UTF_8): String
    fun readAlignedString(charset: Charset = Charsets.UTF_8): String
    fun readInt8Array(size: Int = -1): ByteArray
    fun readUInt8Array(size: Int = -1): Array<UByte>
    fun readInt16Array(size: Int = -1): ShortArray
    fun readUInt16Array(size: Int = -1): Array<UShort>
    fun readInt32Array(size: Int = -1): IntArray
    fun readUInt32Array(size: Int = -1): Array<UInt>
    fun readNestedUInt32Array(size: Int = -1): Array<Array<UInt>>
    fun readInt64Array(size: Int = -1): LongArray
    fun readUInt64Array(size: Int = -1): Array<ULong>
    fun readFloatArray(size: Int = -1): FloatArray
    fun readDoubleArray(size: Int = -1): DoubleArray
    fun readBoolArray(size: Int = -1): BooleanArray
    fun readAlignedStringArray(size: Int = -1): Array<String>
    fun readRect(): Rect
    fun readQuaternion(): Quaternion
    fun readMatrix4x4(): Matrix4x4
    fun readVector2(): Vector2
    fun readVector3(): Vector3
    fun readVector4(): Vector4
    fun readColor(): Color
    fun readMatrix4x4Array(size: Int = -1): Array<Matrix4x4>
    fun readVector2Array(size: Int = -1): Array<Vector2>
    fun readNestedVector2Array(size: Int = -1): Array<Array<Vector2>>
    fun readVector3Array(size: Int = -1): Array<Vector3>
    fun readVector4Array(size: Int = -1): Array<Vector4>
}
