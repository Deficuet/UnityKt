package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.RectTransformImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class RectTransform private constructor(
    private val container: ImplementationContainer<RectTransformImpl>
): Transform(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { RectTransformImpl(ObjectReader(assetFile, info)) })

    val mAnchorMin get() = container.impl.mAnchorMin
    val mAnchorMax get() = container.impl.mAnchorMax
    val mAnchoredPosition get() = container.impl.mAnchoredPosition
    val mSizeDelta get() = container.impl.mSizeDelta
    val mPivot get() = container.impl.mPivot
}