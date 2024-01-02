package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Vector2

interface RectTransform: Transform {
    val mAnchorMin: Vector2
    val mAnchorMax: Vector2
    val mAnchoredPosition: Vector2
    val mSizeDelta: Vector2
    val mPivot: Vector2
}