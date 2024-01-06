package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Canvas
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal class CanvasImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Canvas, CanvasFields(assetFile, info) {
    override val mRenderMode: Int get() {
        checkInitialize()
        return fmRenderMode
    }
    override val mCamera: PPtr<UnityObject> get() {
        checkInitialize()
        return fmCamera
    }
    override val mPlaneDistance: Float get() {
        checkInitialize()
        return fmPlaneDistance
    }
    override val mPixelPerfect: Boolean get() {
        checkInitialize()
        return fmPixelPerfect
    }
    override val mReceivesEvent: Boolean get() {
        checkInitialize()
        return fmReceivesEvent
    }
    override val mOverrideSorting: Boolean get() {
        checkInitialize()
        return fmOverrideSorting
    }
    override val mOverridePixelPerfect: Boolean get() {
        checkInitialize()
        return fmOverridePixelPerfect
    }
    override val mSortingBucketNormalizedSize: Float get() {
        checkInitialize()
        return fmSortingBucketNormalizedSize
    }
    override val mAdditionalShaderChannelsFlag: Int get() {
        checkInitialize()
        return fmAdditionalShaderChannelsFlag
    }
    override val mSortingLayerID: Int get() {
        checkInitialize()
        return fmSortingLayerID
    }
    override val mSortingOrder: Short get() {
        checkInitialize()
        return fmSortingOrder
    }
    override val mTargetDisplay: Byte get() {
        checkInitialize()
        return fmTargetDisplay
    }

    override fun read() {
        super.read()
        fmRenderMode = reader.readInt32()
        fmCamera = PPtrImpl(reader)
        fmPlaneDistance = reader.readFloat()
        fmPixelPerfect = reader.readBool()
        fmReceivesEvent = reader.readBool()
        fmOverrideSorting = reader.readBool()
        fmOverridePixelPerfect = reader.readBool()
        fmSortingBucketNormalizedSize = reader.readFloat()
        fmAdditionalShaderChannelsFlag = reader.readInt32()
        reader.alignStream()
        fmSortingLayerID = reader.readInt32()
        fmSortingOrder = reader.readInt16()
        fmTargetDisplay = reader.readInt8()
    }
}