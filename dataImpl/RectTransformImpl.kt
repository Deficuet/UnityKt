package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

class RectTransformImpl internal constructor(reader: ObjectReader): TransformImpl(reader) {
    val mAnchorMin = reader.readVector2()
    val mAnchorMax = reader.readVector2()
    val mAnchoredPosition = reader.readVector2()
    val mSizeDelta = reader.readVector2()
    val mPivot = reader.readVector2()
}