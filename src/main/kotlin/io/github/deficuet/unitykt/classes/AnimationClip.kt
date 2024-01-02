package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.enums.NumericalEnum
import io.github.deficuet.unitykt.enums.NumericalEnumCompanion
import io.github.deficuet.unitykt.math.Quaternion
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.math.Vector4

interface AnimationClip: NamedObject {
    val mAnimationType: AnimationType
    val mLegacy: Boolean
    val mCompressed: Boolean
    val mUseHighQualityCurve: Boolean
    val mRotationCurves: Array<out QuaternionCurve>
    val mCompressedRotationCurves: Array<out CompressedAnimationCurve>
    val mEulerCurves: Array<out Vector3Curve>
    val mPositionCurves: Array<out Vector3Curve>
    val mScaleCurves: Array<out Vector3Curve>
    val mFloatCurves: Array<out FloatCurve>
    val mPPtrCurves: Array<out PPtrCurve>
    val mSampleRate: Float
    val mWrapMode: Int
    val mBounds: AABB?
    val mMuscleClipSize: UInt
    val mMuscleClip: ClipMuscleConstant?
    val mClipBindingConstant: AnimationClipBindingConstant?
    val mEvents: Array<out AnimationEvent>
}

interface KeyFrame<out T> {
    val time: Float
    val value: T
    val inSlope: T
    val outSlope: T
    val weightedMode: Int
    val inWeight: T?
    val outWeight: T?
}

interface AnimationCurve<out T> {
    val mCurve: Array<out KeyFrame<T>>
    val mPreInfinity: Int
    val mPostInfinity: Int
    val mRotationOrder: Int
}

interface QuaternionCurve {
    val curve: AnimationCurve<Quaternion>
    val path: String
}

interface PackedFloatVector {
    val mNumItems: UInt
    val mRange: Float
    val mStart: Float
    val mData: ByteArray
    val mBitSize: UByte

    fun unpackFloats(
        itemCountInChunk: Int,
        chunkStride: Int,
        start: Int = 0,
        chunkCount: Int = -1
    ): FloatArray
}

interface PackedIntVector {
    val mNumItems: UInt
    val mData: ByteArray
    val mBitSize: UByte

    fun unpackInts(): IntArray
}

interface PackedQuatVector {
    val mNumItems: UInt
    val mData: ByteArray

    fun unpackQuats(): Array<out Quaternion>
}

interface CompressedAnimationCurve {
    val mPath: String
    val mTimes: PackedIntVector
    val mValues: PackedQuatVector
    val mSlopes: PackedFloatVector
    val mPreInfinity: Int
    val mPostInfinity: Int
}

interface Vector3Curve {
    val curve: AnimationCurve<Vector3>
    val path: String
}

interface FloatCurve {
    val curve: AnimationCurve<Float>
    val attribute: String
    val path: String
    val classID: ClassIDType
    val script: PPtr<MonoScript>
}

interface PPtrKeyFrame {
    val time: Float
    val value: PPtr<UnityObject>
}

interface PPtrCurve {
    val curve: Array<out PPtrKeyFrame>
    val attribute: String
    val path: String
    val classID: Int
    val script: PPtr<MonoScript>
}

interface AABB {
    val mCenter: Vector3
    val mExtent: Vector3
}

interface XForm {
    val t: Vector3
    val q: Quaternion
    val s: Vector3
}

interface HandPose {
    val mGrabX: XForm
    val mDoFArray: FloatArray
    val mOverride: Float
    val mCloseOpen: Float
    val mInOut: Float
    val mGrab: Float
}

interface HumanGoal {
    val mX: XForm
    val mWeightT: Float
    val mWeightR: Float
    val mHintT: Vector3
    val mHintWeightT: Float
}

interface HumanPose {
    val mRootX: XForm
    val mLookAt: Vector3
    val mLookAtWeight: Vector4
    val mGoalArray: Array<out HumanGoal>
    val mLeftHandPose: HandPose
    val mRightHandPose: HandPose
    val mDoFArray: FloatArray
    val mTDoFArray: Array<out Vector3>
}

interface StreamedCurveKey {
    val index: Int
    val coeff: FloatArray
    val outSlope: Float
    val value: Float
    val inSlope: Float

    fun nextInSlope(deltaX: Float, rhs: StreamedCurveKey): Float
}

interface StreamedFrame {
    val time: Float
    val keyList: Array<out StreamedCurveKey>
}

interface StreamedClip {
    val data: Array<out UInt>
    val curveCount: UInt

    fun readData(): List<StreamedFrame>
}

interface DenseClip {
    val mFrameCount: Int
    val mCurveCount: UInt
    val mSampleRate: Float
    val mBeginTime: Float
    val mSampleArray: FloatArray
}

interface ConstantClip {
    val data: FloatArray
}

interface ValueConstant {
    val mID: UInt
    val mTypeID: UInt
    val mType: UInt
    val mIndex: UInt
}

interface ValueArrayConstant {
    val mValueArray: Array<out ValueConstant>
}

interface GenericBinding {
    val path: UInt
    val attribute: UInt
    val script: PPtr<UnityObject>?
    val typeID: ClassIDType
    val customType: UByte
    val isPPtrCurve: UByte
    val isIntCurve: UByte
}

interface AnimationClipBindingConstant {
    val genericBindings: Array<out GenericBinding>
    val pptrCurveMapping: Array<out PPtr<UnityObject>>

    fun findBinding(index: Int): GenericBinding?
}

interface Clip {
    val mStreamedClip: StreamedClip
    val mDenseClip: DenseClip
    val mConstantClip: ConstantClip?
    val mBinding: ValueArrayConstant?

    fun buildGenericBindings(): AnimationClipBindingConstant
}

interface ValueDelta {
    val mStart: Float
    val mStop: Float
}


interface ClipMuscleConstant {
    val mDeltaPose: HumanPose
    val mStartX: XForm
    val mStopX: XForm?
    val mLeftFootStartX: XForm
    val mRightFootStartX: XForm
    val mMotionStartX: XForm?
    val mMotionStopX: XForm?
    val mAverageSpeed: Vector3
    val mClip: Clip
    val mStartTime: Float
    val mStopTime: Float
    val mOrientationOffsetY: Float
    val mLevel: Float
    val mCycleOffset: Float
    val mAverageAngularSpeed: Float
    val mIndexArray: IntArray
    val mValueArrayDelta: Array<out ValueDelta>
    val mValueArrayReferencePose: FloatArray
    val mMirror: Boolean
    val mLoopTime: Boolean
    val mLoopBlend: Boolean
    val mLoopBlendOrientation: Boolean
    val mLoopBlendPositionY: Boolean
    val mLoopBlendPositionXZ: Boolean
    val mStartAtOrigin: Boolean
    val mKeepOriginalOrientation: Boolean
    val mKeepOriginalPositionY: Boolean
    val mKeepOriginalPositionXZ: Boolean
    val mHeightFromFeet: Boolean
}

interface AnimationEvent {
    val time: Float
    val functionName: String
    val data: String
    val objectReferenceParameter: PPtr<UnityObject>
    val floatParameter: Float
    val intParameter: Int
    val messageOptions: Int
}

enum class AnimationType(override val id: Int): NumericalEnum<Int> {
    Default(0), Legacy(1), Generic(2), Humanoid(3);

    companion object: NumericalEnumCompanion<Int, AnimationType>(values(), Default)
}
