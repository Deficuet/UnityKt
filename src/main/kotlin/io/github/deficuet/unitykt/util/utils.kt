package io.github.deficuet.unitykt.util

import java.nio.charset.Charset

internal fun ByteArray.rearrange(endian: EndianType): ByteArray {
    return if (size > 1 && endian == EndianType.LittleEndian) reversedArray() else this
}

internal fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8) =
    java.lang.String(this, charset) as String