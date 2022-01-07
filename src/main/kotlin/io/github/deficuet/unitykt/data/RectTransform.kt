package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class RectTransform internal constructor(reader: ObjectReader): Transform(reader) {
    val mAnchorMin = reader.readVector2()
    val mAnchorMax = reader.readVector2()
    val mAnchoredPosition = reader.readVector2()
    val mSizeDelta = reader.readVector2()
    val mPivot = reader.readVector2()
}