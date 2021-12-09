package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.util.EndianByteArrayReader

fun main() {
    val t = EndianByteArrayReader(
        array = byteArrayOf(0x0, 0x0, 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7)
    )
    println(t.position)
    t.position = 0
    println(t.read(1)[0])
    println(t.position)
}