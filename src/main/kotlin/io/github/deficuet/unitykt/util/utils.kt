package io.github.deficuet.unitykt.util

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

internal fun ByteArray.rearrange(endian: EndianType): ByteArray {
    return if (size > 1 && endian == EndianType.LittleEndian) reversedArray() else this
}

internal fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8) =
    java.lang.String(this, charset) as String

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

internal infix fun Boolean.imply(other: Boolean) = !this or other

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