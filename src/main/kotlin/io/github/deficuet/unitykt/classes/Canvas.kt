package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

/**
 * Only tested under version `2018.4.34f1`
 */
interface Canvas: Behaviour {
    val mRenderMode: Int
    val mCamera: PPtr<UnityObject>      //PPtr<Camera>
    val mPlaneDistance: Float
    val mPixelPerfect: Boolean
    val mReceivesEvent: Boolean
    val mOverrideSorting: Boolean
    val mOverridePixelPerfect: Boolean
    val mSortingBucketNormalizedSize: Float
    val mAdditionalShaderChannelsFlag: Int
    val mSortingLayerID: Int
    val mSortingOrder: Short
    val mTargetDisplay: Byte
}