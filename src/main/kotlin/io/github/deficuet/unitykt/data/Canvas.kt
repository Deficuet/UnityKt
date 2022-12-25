package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.CanvasImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

/**
 *  Errors may occur for bundles with version before `2018.4.34f1`
 */
class Canvas private constructor(
    private val container: ImplementationContainer<CanvasImpl>
): Behaviour(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
            this(ImplementationContainer(assetFile, info) { CanvasImpl(ObjectReader(assetFile, info)) })

    val mRenderMode: Int                            get() = container.impl.mRenderMode
    val mCamera: PPtr<Object>                       get() = container.impl.mCamera
    val mPlaneDistance: Float                       get() = container.impl.mPlaneDistance
    val mPixelPerfect: Boolean                      get() = container.impl.mPixelPerfect
    val mReceivesEvent: Boolean                     get() = container.impl.mReceivesEvent
    val mOverrideSorting: Boolean                   get() = container.impl.mOverrideSorting
    val mOverridePixelPerfect: Boolean              get() = container.impl.mOverridePixelPerfect
    val mSortingBucketNormalizedSize: Float         get() = container.impl.mSortingBucketNormalizedSize
    val mAdditionalShaderChannelsFlag: Int          get() = container.impl.mAdditionalShaderChannelsFlag
    val mSortingLayerID: Int                        get() = container.impl.mSortingLayerID
    val mSortingOrder: Short                        get() = container.impl.mSortingOrder
    val mTargetDisplay: Byte                        get() = container.impl.mTargetDisplay
}