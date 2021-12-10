package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.util.EndianByteArrayReader
import io.github.deficuet.unitykt.util.EndianFileStreamReader
import java.io.FileInputStream

fun main() {
    val t = EndianByteArrayReader(
        byteArrayOf(0,0,0,0,0,0,0)
    )


//    val a = FileInputStream("D:\\Programs\\srcode\\test\\未命名3")
//    println(a.channel.size())
//    println(with(a.channel) { position() >= size() })
//    a.close()
}