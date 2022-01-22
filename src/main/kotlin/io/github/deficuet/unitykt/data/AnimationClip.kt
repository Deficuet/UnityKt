package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.*
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AnimationClip private constructor(
    private val container: ImplementationContainer<AnimationClipImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AnimationClipImpl(ObjectReader(assetFile, info)) })

    val mAnimationType: AnimationType                               get() = container.impl.mAnimationType
    val mLegacy: Boolean                                            get() = container.impl.mLegacy
    val mCompressed: Boolean                                        get() = container.impl.mCompressed
    val mUseHighQualityCurve: Boolean                               get() = container.impl.mUseHighQualityCurve
    val mRotationCurves: Array<QuaternionCurve>                     get() = container.impl.mRotationCurves
    val mCompressedRotationCurves: Array<CompressedAnimationCurve>  get() = container.impl.mCompressedRotationCurves
    val mEulerCurves: Array<Vector3Curve>                           get() = container.impl.mEulerCurves
    val mPositionCurves: Array<Vector3Curve>                        get() = container.impl.mPositionCurves
    val mScaleCurves: Array<Vector3Curve>                           get() = container.impl.mScaleCurves
    val mFloatCurves: Array<FloatCurve>                             get() = container.impl.mFloatCurves
    val mPPtrCurves: Array<PPtrCurve>                               get() = container.impl.mPPtrCurves
    val mSampleRate: Float                                          get() = container.impl.mSampleRate
    val mWrapMode: Int                                              get() = container.impl.mWrapMode
    val mBounds: AABB?                                              get() = container.impl.mBounds
    val mMuscleClipSize: UInt                                       get() = container.impl.mMuscleClipSize
    val mMuscleClip: ClipMuscleConstant?                            get() = container.impl.mMuscleClip
    val mClipBindingConstant: AnimationClipBindingConstant?         get() = container.impl.mClipBindingConstant
    val mEvents: Array<AnimationEvent>                              get() = container.impl.mEvents
}