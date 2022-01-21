package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*
import io.github.deficuet.unitykt.dataImpl.TextureFormat
import java.io.File
import javax.imageio.ImageIO

fun main() {
    AssetManager.loadFolderRecursively("F:\\CS30Final\\example")
    val texs = AssetManager.objects.allObjectsOf<Texture2D>()
    val etc2rgba8 = texs.first { it.mTextureFormat == TextureFormat.ETC2_RGBA8 }
    ImageIO.write(etc2rgba8.image, "png", File("F:\\UnityKt\\build\\libs\\artifacts\\unitykt_main_jar\\etc2rgb8a.png"))
    val astc = texs.first { it.mTextureFormat == TextureFormat.ASTC_RGBA_8x8 }
    ImageIO.write(astc.image, "png", File("F:\\UnityKt\\build\\libs\\artifacts\\unitykt_main_jar\\astc8x8.png"))
}