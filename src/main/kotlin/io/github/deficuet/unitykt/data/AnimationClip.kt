package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AnimationClipImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AnimationClip private constructor(
    private val container: ImplementationContainer<AnimationClipImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AnimationClipImpl(ObjectReader(assetFile, info)) })

    val mAnimationType get() = container.impl.mAnimationType
    val mLegacy get() = container.impl.mLegacy
    val mCompressed get() = container.impl.mCompressed
    val mUseHighQualityCurve get() = container.impl.mUseHighQualityCurve
    val mRotationCurves get() = container.impl.mRotationCurves
    val mCompressedRotationCurves get() = container.impl.mCompressedRotationCurves
    val mEulerCurves get() = container.impl.mEulerCurves
    val mPositionCurves get() = container.impl.mPositionCurves
    val mScaleCurves get() = container.impl.mScaleCurves
    val mFloatCurves get() = container.impl.mFloatCurves
    val mPPtrCurves get() = container.impl.mPPtrCurves
    val mSampleRate get() = container.impl.mSampleRate
    val mWrapMode get() = container.impl.mWrapMode
    val mBounds get() = container.impl.mBounds
    val mMuscleClipSize get() = container.impl.mMuscleClipSize
    val mMuscleClip get() = container.impl.mMuscleClip
    val mClipBindingConstant get() = container.impl.mClipBindingConstant
    val mEvents get() = container.impl.mEvents
}