package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.file.BundleFile
import io.github.deficuet.unitykt.util.CompressUtils
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianFileStreamReader
import io.github.deficuet.unitykt.util.sum
import java.io.File

fun main() {
    val b = BundleFile(EndianFileStreamReader("F:\\CS30Final\\example\\baoduoliuhua"))
////    val c = BundleFile(EndianFileStreamReader("D:/leidian/Painting/baoduoliuhua_tex"))
    val dump = File("F:\\CS30Final\\dump\\baoduoliuhua_dump")
    dump.writeBytes((b.files["CAB-8aa23d255bf6b144eeeacdf7c9936504"] as EndianBinaryReader).bytes)
    println(b.files)
}