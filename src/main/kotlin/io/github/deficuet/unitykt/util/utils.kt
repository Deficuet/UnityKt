package io.github.deficuet.unitykt.util

import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

internal fun Byte.toIntBits() = toUByte().toInt()

internal fun Short.toIntBits() = toUShort().toInt()

internal operator fun ByteArray.get(i: UInt) = get(i.toInt()).toIntBits()

internal operator fun ByteArray.get(i: Int, l: Int) = sliceArray(i until i + l)

internal fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8) =
    java.lang.String(this, charset).toString()

internal fun ByteArray.toHalf(): Float {
    if (size != 2) throw IllegalStateException("There should be 2 bytes only")
    val intValue = this[0].toIntBits().shl(8).or(this[1].toIntBits())
    var mantissa = intValue.and(0x03FF)
    var exp = intValue.and(0x7C00)
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
            mantissa = mantissa.shl(1)
            exp -= 0x400
        } while (mantissa.and(0x400) == 0)
        mantissa = mantissa.and(0x3FF)
    }
    return Float.fromBits(
        intValue.and(0x8000).shl(16).or(exp.or(mantissa).shl(13))
    )
}

internal fun ByteArray.toChar(): Char {
    if (size != 2) throw IllegalStateException("There should be 2 bytes only")
    return Char(
        ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).short.toUShort()
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

internal fun Int.clampByte(): Byte {
    val i = this
    return with(Byte.Companion) {
        if (i > MAX_VALUE) MAX_VALUE else if (i < MIN_VALUE) MIN_VALUE else i.toByte()
    }
}

internal fun String.isFile(): Boolean = Files.isRegularFile(Path(this))

internal fun String.isDirectory(): Boolean = Files.isDirectory(Path(this))

internal operator fun String.invoke(v: Any) = format(v)

@PublishedApi internal fun String.listFiles(): List<String> {
    return Files.newDirectoryStream(Path(this)).use { stream ->
        stream.filter { it.isRegularFile() }.map { it.name }
    }
}

@PublishedApi internal fun <V> Map<String, V>.tryGet(key: String): V? {
    return this[key] ?: this[key.uppercase()]
}

@PublishedApi internal fun List<String>.containsIgnoreCase(element: String): String? {
    return find { it.contentEquals(element) }
}

internal fun List<ByteArray>.sum(): ByteArray {
    var bytes = kotlin.byteArrayOf()
    forEach { bytes += it }
    return bytes
}

internal fun <T> T.equalsAnyOf(vararg other: T): Boolean {
    for (v in other) {
        if (this == v) return true
    }
    return false
}

internal fun Map<*, *>.toJSONObject(): JSONObject {
    return JSONObject().apply {
        for (entry in this@toJSONObject) {
            val key = java.lang.String.valueOf(entry.key)
            val value = entry.value!!
            if (value::class.java.isArray) {
                put(key, JSONArray(value))
                continue
            }
            when (value) {
                is Collection<*> -> put(key, JSONArray(value))
                is Map<*, *> -> put(key, value.toJSONObject())
                else -> put(key, value)
            }
        }
    }
}