package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val b = AssetManager.loadFile("D:/leidian/Painting\\baoduoliuhua_tex")
    val tex = b.objects.firstObjectOf<Texture2D>()
    println(tex.mTextureFormat)
    ImageIO.write(
        tex.image, "png",
        File("D:\\Programs\\srcode\\kotlin\\UnityKt\\out\\test.png")
    )
}