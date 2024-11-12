package io.github.deficuet.unitykt.utils

import java.io.Closeable
import java.nio.ByteOrder

abstract class EndianReaderTemplate: Closeable, DataInput {
    abstract val bytes: ByteArray
    abstract var position: Long
    abstract val length: Long
    abstract var endian: ByteOrder

    abstract val baseOffset: Long

    abstract val ignoredOffset: Long

    var absolutePosition: Long
        get() = position + ignoredOffset
        set(value) { position = value - ignoredOffset }

    val realOffset get() = position + baseOffset

    abstract fun read(): Int
    abstract fun read(buf: ByteArray): Int
    abstract fun read(size: Int): ByteArray

    fun skip(n: Int): EndianReaderTemplate {
        position += n
        return this
    }

    fun skip(n: Long): EndianReaderTemplate {
        position += n
        return this
    }

    open fun alignStream(alignment: Int = 4) {
        skip((alignment - position % alignment) % alignment)
    }
}

inline fun <R: EndianReaderTemplate, reified T> R.readArrayOf(
    size: Int = -1,
    crossinline constructor: R.() -> T
): Array<T> {
    val num = if (size == -1) readInt32() else size
    return Array(num) { constructor() }
}

inline fun <R: EndianReaderTemplate, reified T> R.readArrayIndexedOf(
    size: Int = -1,
    crossinline constructor: R.(Int) -> T
): Array<T> {
    val num = if (size == -1) readInt32() else size
    return Array(num) { constructor(it) }
}

inline fun <R: EndianReaderTemplate, T> R.withMark(crossinline block: R.() -> T): T {
    val mark = position
    val result = this.block()
    position = mark
    return result
}

inline fun <R: EndianReaderTemplate, T> R.runThenReset(crossinline block: R.() -> T): T {
    val result = this.block()
    position = 0
    return result
}

inline fun <R: EndianReaderTemplate, T> R.useEndian(e: ByteOrder, crossinline block: R.() -> T): T {
    val cache = endian
    endian = e
    val result = this.block()
    endian = cache
    return result
}
