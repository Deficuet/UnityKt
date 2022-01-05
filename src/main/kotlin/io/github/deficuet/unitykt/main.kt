package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*
import io.github.deficuet.unitykt.extension.Color
import io.github.deficuet.unitykt.extension.ETCDecoder
import java.awt.image.*
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun main() {
//    //D:\ALFiles\assets\AssetBundles\paintingface\hailunna_2
    val a = AssetManager.loadFile("D:\\ALFiles\\assets\\AssetBundles\\paintingface\\hailunna_2")
    val tex = a.objects.firstObjectOf<Texture2D>()
    val d = tex.decompressedImageData

    ImageIO.write(tex.image, "png", File("D:\\Programs\\srcode\\test\\test.png"))
}