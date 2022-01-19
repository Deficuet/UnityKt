package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.Texture2DImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Texture2D private constructor(
    private val container: ImplementationContainer<Texture2DImpl>
): Texture(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { Texture2DImpl(ObjectReader(assetFile, info)) })

    val mWidth get() = container.impl.mWidth
    val mHeight get() = container.impl.mHeight
    val mTextureFormat get() = container.impl.mTextureFormat
    val mMipMap get() = container.impl.mMipMap
    val mMipCount get() = container.impl.mMipCount
    val mTextureSettings get() = container.impl.mTextureSettings
    val imageData get() = container.impl.imageData
    val mStreamData get() = container.impl.mStreamData

    val decompressedImageData get() = container.impl.decompressedImageData

    /**
     * Usually up-side-down
     */
    val image get() = container.impl.image
}