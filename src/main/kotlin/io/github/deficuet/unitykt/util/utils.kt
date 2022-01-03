package io.github.deficuet.unitykt.util

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import io.github.deficuet.unitykt.data.Object

internal fun Byte.toIntBits() = toUByte().toInt()

internal fun ByteArray.rearrange(endian: EndianType): ByteArray {
    return if (size > 1 && endian == EndianType.LittleEndian) reversedArray() else this
}

internal fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8) =
    java.lang.String(this, charset) as String

internal fun ByteArray.toHalf(): Float {
    if (size != 2) throw IllegalStateException("There should be 2 bytes only")
    val intValue = this[0].toIntBits().shl(8).or(this[1].toIntBits())
    var mantissa = intValue and 0x03FF
    var exp = intValue and 0x7C00
    if (exp == 0x7C00) exp = 0x3FC00
    else if (exp != 0) {
        exp += 0x1C000
        if (mantissa == 0 && exp > 0x1C400) {
            return Float.fromBits(
                intValue.and(0x8000).shl(16).or(exp.shl(13)).or(0x3FF)
            )
        }
    } else if (mantissa != 0) {
        exp = 0x1C400
        do {
            mantissa = mantissa shl 1
            exp -= 0x400
        } while (mantissa and 0x400 == 0)
        mantissa = mantissa and 0x3FF
    }
    return Float.fromBits(
        intValue.and(0x8000).shl(16).or(exp.or(mantissa).shl(13))
    )
}

internal fun byteArrayOf(vararg bytes: Int) = ByteArray(bytes.size) { bytes[it].toByte() }

internal fun byteArrayOf(str: String) = ByteArray(str.length) { str[it].code.toByte() }

internal operator fun IntArray.compareTo(other: IntArray): Int {
    for (i in 0 until minOf(size, other.size)) {
        val result = get(i).compareTo(other[i])
        if (result != 0) return result
    }
    return size.compareTo(other.size)
}

internal fun String.isFile(): Boolean = Files.isRegularFile(Path.of(this))

internal fun String.isDirectory(): Boolean = Files.isDirectory(Path.of(this))

internal fun String.listFiles(): List<String> {
    return Files.newDirectoryStream(Path.of(this)).use { stream ->
        stream.filter { it.isRegularFile() }.map { it.name }
    }
}

internal fun List<String>.containsIgnoreCase(element: String, sRef: StringRef): Boolean {
    return find { it.contentEquals(element) }?.also { sRef.value = it } != null
}

//internal infix fun Boolean.imply(other: Boolean) = !this or other

internal fun List<ByteArray>.sum(): ByteArray {
    var bytes = kotlin.byteArrayOf()
    forEach { bytes += it }
    return bytes
}

internal fun <V> Map<String, V>.tryGetOrUppercase(key: String): V? {
    return if (key in this) {
        getValue(key)
    } else if (key.uppercase() in this) {
        getValue(key.uppercase())
    } else {
        null
    }
}