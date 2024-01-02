package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.RectTransform
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Vector2

internal class RectTransformImpl(
    assetFile: SerializedFile, info: ObjectInfo
): RectTransform, RectTransformFields(assetFile, info) {
    override val mAnchorMin: Vector2 get() {
        checkInitialize()
        return fmAnchorMin
    }
    override val mAnchorMax: Vector2 get() {
        checkInitialize()
        return fmAnchorMax
    }
    override val mAnchoredPosition: Vector2 get() {
        checkInitialize()
        return fmAnchoredPosition
    }
    override val mSizeDelta: Vector2 get() {
        checkInitialize()
        return fmSizeDelta
    }
    override val mPivot: Vector2 get() {
        checkInitialize()
        return fmPivot
    }

    override fun read() {
        super.read()
        fmAnchorMin = reader.readVector2()
        fmAnchorMax = reader.readVector2()
        fmAnchoredPosition = reader.readVector2()
        fmSizeDelta = reader.readVector2()
        fmPivot = reader.readVector2()
    }
}