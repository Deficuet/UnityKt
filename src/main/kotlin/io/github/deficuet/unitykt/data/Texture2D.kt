package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.GLTextureSettings
import io.github.deficuet.unitykt.dataImpl.StreamingInfo
import io.github.deficuet.unitykt.dataImpl.Texture2DImpl
import io.github.deficuet.unitykt.dataImpl.TextureFormat
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.ResourceReader
import java.awt.image.BufferedImage

class Texture2D private constructor(
    private val container: ImplementationContainer<Texture2DImpl>
): Texture(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { Texture2DImpl(ObjectReader(assetFile, info)) })

    val mWidth: Int                             get() = container.impl.mWidth
    val mHeight: Int                            get() = container.impl.mHeight
    val mTextureFormat: TextureFormat           get() = container.impl.mTextureFormat
    val mMipMap: Boolean                        get() = container.impl.mMipMap
    val mMipCount: Int                          get() = container.impl.mMipCount
    val mTextureSettings: GLTextureSettings     get() = container.impl.mTextureSettings
    val imageData: ResourceReader               get() = container.impl.imageData
    val mStreamData: StreamingInfo?             get() = container.impl.mStreamData

    val decompressedImageData: ByteArray        get() = container.impl.decompressedImageData

    /**
     * Usually up-side-down
     */
    val image: BufferedImage                    get() = container.impl.image
}