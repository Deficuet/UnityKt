package io.github.deficuet.unitykt.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

internal operator fun ByteArray.get(i: UInt) = get(i.toInt()).toUByte().toInt()

internal operator fun ByteArray.get(i: Int, l: Int) = sliceArray(i until i + l)

/**
 * `0u` will return `true`
 */
internal fun isPowerOf2(x: UInt) = x.and(x - 1u) == 0u

internal fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8) =
    java.lang.String(this, charset).toString()

internal fun parseHalf(data: ByteArray, pos: UInt): Float {
    val intValue = data[pos].shl(8).or(data[pos+1u])
    var mantissa = intValue.and(0x03FF)
    var exp = intValue.and(0x7C00)
    if (exp == 0x7C00) {
        exp = 0x3FC00
    } else if (exp != 0) {
        exp += 0x1C000
        if (mantissa == 0 && exp > 0x1C400) {
            return Float.fromBits(
                intValue.and(0x8000).shl(16).or(exp.shl(13)).or(0x3FF)
            )
        }
    } else if (mantissa != 0) {
        exp = 0x1C400
        do {
            mantissa = mantissa.shl(1)
            exp -= 0x400
        } while (mantissa.and(0x400) == 0)
        mantissa = mantissa.and(0x3FF)
    }
    return Float.fromBits(
        intValue.and(0x8000).shl(16).or(exp.or(mantissa).shl(13))
    )
}

internal fun parseInt(data: ByteArray, pos: UInt): Int {
    return data[pos].shl(24).or(data[pos+1u].shl(16)).or(data[pos+2u].shl(8)).or(data[pos+3u])
}

internal fun parseFloat(data: ByteArray, pos: UInt) = Float.fromBits(parseInt(data, pos))

internal fun parseUInt16(data: ByteArray, pos: UInt): Int {
    return data[pos].shl(8).or(data[pos+1u])
}

internal fun parseDownScaledInt8(data: ByteArray, pos: UInt): Byte {
    return ((parseUInt16(data, pos) * 255) + 32895).shr(16).toByte()
}

internal fun ByteArray.toChar(): Char {
    if (size != 2) throw IllegalStateException("There should be 2 bytes only")
    return Char(
        ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).short.toUShort()
    )
}

internal operator fun IntArray.compareTo(other: IntArray): Int {
    for (i in 0 until minOf(size, other.size)) {
        val result = get(i).compareTo(other[i])
        if (result != 0) return result
    }
    return size.compareTo(other.size)
}

internal fun Int.clampByte(): Byte {
    return (if (this > 255) 255 else if (this < 0) 0 else this).toByte()
}
