package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*

fun main() {
//    //D:\ALFiles\assets\AssetBundles\paintingface\hailunna_2
    val a = AssetManager.loadFile("D:/leidian/Painting/baoduoliuhua")

    val rect = a.objects.firstObjectOf<RectTransform>()
    println(rect.dump())

//    val tex = a.objects.firstObjectOf<Texture2D>()
//    ImageIO.write(tex.image, "png", File("D:\\Programs\\srcode\\test\\test.png"))
}