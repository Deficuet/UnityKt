package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.SpriteAtlasData
import io.github.deficuet.unitykt.dataImpl.SpriteAtlasImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class SpriteAtlas private constructor(
    private val container: ImplementationContainer<SpriteAtlasImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { SpriteAtlasImpl(ObjectReader(assetFile, info)) })

    val mPackedSprites: Array<PPtr<Sprite>>                             get() = container.impl.mPackedSprites
    val mRenderDataMap: Map<Pair<ByteArray, Long>, SpriteAtlasData>     get() = container.impl.mRenderDataMap
    val mIsVariant: Boolean                                             get() = container.impl.mIsVariant
}