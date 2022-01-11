package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*

fun main() {
//    //D:\ALFiles\assets\AssetBundles\paintingface\hailunna_2
    val a = AssetManager.loadFile("F:\\CS30Final\\example\\baoduoliuhua")

    val rect = a.objects.firstObjectOf<AssetBundle>()
    println(rect.dump())
    val go = a.objects.objectFromPathID(1361680547961059170)
    println(go!!.dump())
//    val tex = a.objects.firstObjectOf<Texture2D>()
//    ImageIO.write(tex.image, "png", File("D:\\Programs\\srcode\\test\\test.png"))
}