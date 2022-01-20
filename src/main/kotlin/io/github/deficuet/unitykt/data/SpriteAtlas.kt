package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.SpriteAtlasImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class SpriteAtlas private constructor(
    private val container: ImplementationContainer<SpriteAtlasImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { SpriteAtlasImpl(ObjectReader(assetFile, info)) })

    val mPackedSprites get() = container.impl.mPackedSprites
    val mRenderDataMap get() = container.impl.mRenderDataMap
    val mIsVariant get() = container.impl.mIsVariant
}