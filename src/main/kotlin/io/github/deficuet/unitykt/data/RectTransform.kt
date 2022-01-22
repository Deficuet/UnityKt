package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.RectTransformImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.util.ObjectReader

class RectTransform private constructor(
    private val container: ImplementationContainer<RectTransformImpl>
): Transform(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { RectTransformImpl(ObjectReader(assetFile, info)) })

    val mAnchorMin: Vector2         get() = container.impl.mAnchorMin
    val mAnchorMax: Vector2         get() = container.impl.mAnchorMax
    val mAnchoredPosition: Vector2  get() = container.impl.mAnchoredPosition
    val mSizeDelta: Vector2         get() = container.impl.mSizeDelta
    val mPivot: Vector2             get() = container.impl.mPivot
}