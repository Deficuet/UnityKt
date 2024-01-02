package io.github.deficuet.unitykt.util

import java.io.ByteArrayInputStream

class ByteArrayReader(
    array: ByteArray, offset: Int, length: Int
): ByteArrayInputStream(array, offset, length) {
    constructor(array: ByteArray): this(array, 0, array.size)
    constructor(array: ByteArray, offset: Int): this(array, offset, array.size)

    fun seek(newPos: Int) {
        pos = newPos
    }

    fun tell(): Int = pos
}