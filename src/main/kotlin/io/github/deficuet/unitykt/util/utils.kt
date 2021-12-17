package io.github.deficuet.unitykt.util

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

internal fun ByteArray.rearrange(endian: EndianType): ByteArray {
    return if (size > 1 && endian == EndianType.LittleEndian) reversedArray() else this
}

internal fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8) =
    java.lang.String(this, charset) as String

internal fun String.isFile(): Boolean = Files.isRegularFile(Path.of(this))

internal fun byteArrayOf(vararg bytes: Int) = ByteArray(bytes.size) { bytes[it].toByte() }

internal fun byteArrayOf(str: String) = ByteArray(str.length) { str[it].code.toByte() }

internal infix fun Boolean.imply(other: Boolean) = !this or other

internal fun List<ByteArray>.sum(): ByteArray {
    var bytes = kotlin.byteArrayOf()
    forEach { bytes += it }
    return bytes
}