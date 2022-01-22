package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.SpriteImpl
import io.github.deficuet.unitykt.dataImpl.SpriteRenderData
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.math.Rectangle
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.util.ObjectReader

class Sprite private constructor(
    private val container: ImplementationContainer<SpriteImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { SpriteImpl(ObjectReader(assetFile, info)) })

    val mRect: Rectangle                        get() = container.impl.mRect
    val mOffset: Vector2                        get() = container.impl.mOffset
    val mBorder: Vector4                        get() = container.impl.mBorder
    val mPixelsToUnits: Float                   get() = container.impl.mPixelsToUnits
    val mPivot: Vector2                         get() = container.impl.mPivot
    val mExtrude: UInt                          get() = container.impl.mExtrude
    val mIsPolygon: Boolean                     get() = container.impl.mIsPolygon
    val mRenderDataKey: Map<ByteArray, Long>    get() = container.impl.mRenderDataKey
    val mAtlasTags: Array<String>               get() = container.impl.mAtlasTags
    val mSpriteAtlas: PPtr<SpriteAtlas>?        get() = container.impl.mSpriteAtlas
    val mRD: SpriteRenderData                   get() = container.impl.mRD
    val mPhysicsShape: Array<Array<Vector2>>    get() = container.impl.mPhysicsShape
}