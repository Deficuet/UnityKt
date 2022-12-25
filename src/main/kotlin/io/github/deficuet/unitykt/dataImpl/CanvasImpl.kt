package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.*
import io.github.deficuet.unitykt.util.ObjectReader

/**
 *  Errors may occur for bundles with version before `2018.4.34f1`
 */
class CanvasImpl internal constructor(reader: ObjectReader): BehaviourImpl(reader) {
    val mRenderMode = reader.readInt()
    val mCamera = PPtr<Object>(reader)          //PPtr<Camera>
    val mPlaneDistance = reader.readFloat()
    val mPixelPerfect = reader.readBool()
    val mReceivesEvent = reader.readBool()
    val mOverrideSorting = reader.readBool()
    val mOverridePixelPerfect = reader.readBool()
    val mSortingBucketNormalizedSize = reader.readFloat()
    val mAdditionalShaderChannelsFlag = reader.readInt()
    val mSortingLayerID: Int
    val mSortingOrder: Short
    val mTargetDisplay: Byte

    init {
        reader.alignStream()
        mSortingLayerID = reader.readInt()
        mSortingOrder = reader.readShort()
        mTargetDisplay = reader.readSByte()
    }
}