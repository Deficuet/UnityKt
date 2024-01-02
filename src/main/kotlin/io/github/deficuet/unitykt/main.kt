package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.classes.Sprite
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val tm = System.currentTimeMillis()
    val m = UnityAssetManager.new()
    val c = m.loadFile("C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles\\paintingface\\safuke")
//    val d = m.loadFile("C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles\\paintingface\\safuke_xinshou")
    val root = "C:\\Users\\Defic\\self\\Programs\\srcode\\py\\AzurLaneScriptsDecode\\temp"
    val i = c.objectMap.getAs<Sprite>(-3269655441389158635).getImage()
    assert(i != null)
    println(System.currentTimeMillis() - tm)
    ImageIO.write(i, "png", File("$root/safuke8.png"))
}

