package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.SpriteImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Sprite private constructor(
    private val container: ImplementationContainer<SpriteImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { SpriteImpl(ObjectReader(assetFile, info)) })

    val mRect get() = container.impl.mRect
    val mOffset get() = container.impl.mOffset
    val mBorder get() = container.impl.mBorder
    val mPixelsToUnits get() = container.impl.mPixelsToUnits
    val mPivot get() = container.impl.mPivot
    val mExtrude get() = container.impl.mExtrude
    val mIsPolygon get() = container.impl.mIsPolygon
    val mRenderDataKey get() = container.impl.mRenderDataKey
    val mAtlasTags get() = container.impl.mAtlasTags
    val mSpriteAtlas get() = container.impl.mSpriteAtlas
    val mRD get() = container.impl.mRD
    val mPhysicsShape get() = container.impl.mPhysicsShape
}