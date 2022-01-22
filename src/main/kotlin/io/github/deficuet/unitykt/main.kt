package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Mesh

fun main() {
//    AssetManager.loadFolderRecursively("F:\\CS30Final\\example")
//    val texs = AssetManager.objects.allObjectsOf<Texture2D>()
//    val etc2rgba8 = texs.first { it.mTextureFormat == TextureFormat.ETC2_RGBA8 }
//    ImageIO.write(etc2rgba8.image, "png", File("F:\\UnityKt\\build\\libs\\artifacts\\unitykt_main_jar\\etc2rgb8a.png"))
//    val astc = texs.first { it.mTextureFormat == TextureFormat.ASTC_RGBA_8x8 }
//    ImageIO.write(astc.image, "png", File("F:\\UnityKt\\build\\libs\\artifacts\\unitykt_main_jar\\astc8x8.png"))
    val b = AssetManager.loadFile("D:\\leidian\\example\\ASTCRGB-456810\\prologdepengroup-assets-_mx-characters-hasumi_original-_mxprolog-2021-06-10_assets_all_1882fcb80d5fb5cc4a4363e3b1fdfca3.bundle")
//    val b = AssetManager.loadFile("D:/leidian/Painting/baoduoliuhua_tex")
    val m = b.objects.firstObjectOf<Mesh>()
    println(m.exportFaces[0].contentToString())
}