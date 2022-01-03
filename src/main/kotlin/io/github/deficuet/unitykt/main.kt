package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*
import java.awt.image.*
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val a = AssetManager.loadFile("D:/leidian/Painting/baoduoliuhua_tex")
    val tex = a.objects.firstObjectOf<Texture2D>()
    val id = tex.imageData.bytes
    val d = tex.decompressedImageData

    val data = DataBufferByte(d, d.size)
    val i = BufferedImage(tex.mWidth, tex.mHeight, BufferedImage.TYPE_4BYTE_ABGR)
    val module = ComponentSampleModel(
        DataBuffer.TYPE_BYTE, tex.mWidth, tex.mHeight, 4, tex.mWidth * 4, intArrayOf(0, 1, 2, 3)
    )
    val r = Raster.createRaster(module, data, null)
    i.data = r
//    id.forEach { println(it) }
//    println(i == null)
    ImageIO.write(i, "png", File("D:\\Programs\\srcode\\test\\test.png"))
}