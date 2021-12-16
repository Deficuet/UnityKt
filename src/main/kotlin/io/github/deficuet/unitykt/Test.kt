package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.file.BundleFile
import io.github.deficuet.unitykt.util.EndianFileStreamReader

fun main() {
//    val b = BundleFile(EndianFileStreamReader("F:\\example\\baoduoliuhua_n"))
    val c = BundleFile(EndianFileStreamReader("D:/leidian/Painting/baoduoliuhua_tex"))
    println(c.files)
}