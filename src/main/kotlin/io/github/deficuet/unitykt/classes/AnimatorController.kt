package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.pptr.PPtr

interface AnimatorController: RuntimeAnimatorController {
    val mControllerSize: UInt
    val mController: ControllerConstant
    val mTOS: Map<UInt, List<String>>
    val mAnimationClip: Array<out PPtr<AnimationClip>>
}

interface HumanPoseMask {
    val word0: UInt
    val word1: UInt
    val word2: UInt
}

interface SkeletonMaskElement {
    val mPathHash: UInt
    val mWeight: Float
}

interface SkeletonMask {
    val mData: Array<out SkeletonMaskElement>
}

interface LayerConstant {
    val mStateMachineIndex: UInt
    val mStateMachineMotionSetIndex: UInt
    val mBodyMask: HumanPoseMask
    val mSkeletonMask: SkeletonMask
    val mBinding: UInt
    val mLayerBlendingMode: Int
    val mDefaultWeight: Float
    val mIKPass: Boolean
    val mSyncedLayerAffectsTiming: Boolean
}

interface ConditionConstant {
    val mConditionMode: UInt
    val mEventID: UInt
    val mEventThreshold: Float
    val mExitTime: Float
}

interface TransitionConstant {
    val mConditionConstantArray: Array<out ConditionConstant>
    val mDestinationState: UInt
    val mFullPathID: UInt
    val mID: UInt
    val mUserID: UInt
    val mTransitionDuration: Float
    val mTransitionOffset: Float
    val mExitTime: Float
    val mHasExitTime: Boolean
    val mHasFixedDuration: Boolean
    val mInterruptionSource: Int
    val mOrderedInterruption: Boolean
    val mAtomic: Boolean
    val mCanTransitionToSelf: Boolean
}

interface LeafInfoConstant {
    val mIDArray: Array<out UInt>
    val mIndexOffset: UInt
}

interface MotionNeighborList {
    val mNeighborArray: Array<out UInt>
}

interface Blend2dDataConstant {
    val mChildPositionArray: Array<out Vector2>
    val mChildMagnitudeArray: FloatArray
    val mChildPairVectorArray: Array<out Vector2>
    val mChildPairAvgMagInvArray: FloatArray
    val mChildNeighborListArray: Array<out MotionNeighborList>
}

interface Blend1dDataConstant {
    val mChildThresholdArray: FloatArray
}

interface BlendDirectDataConstant {
    val mChildBlendEventIDArray: Array<out UInt>
    val mNormalizedBlendValues: Boolean
}

interface BlendTreeNodeConstant {
    val mBlendType: UInt
    val mBlendEventID: UInt
    val mBlendEventYID: UInt
    val mChildIndices: Array<out UInt>
    val mChildThresholdArray: FloatArray
    val mBlend1dData: Blend1dDataConstant?
    val mBlend2dData: Blend2dDataConstant?
    val mBlendDirectData: BlendDirectDataConstant?
    val mClipID: UInt
    val mClipIndex: UInt
    val mDuration: Float
    val mCycleOffset: Float
    val mMirror: Boolean
}

interface BlendTreeConstant {
    val mNodeArray: Array<out BlendTreeNodeConstant>
    val mBlendEventArrayConstant: ValueArrayConstant?
}

interface StateConstant {
    val mTransitionConstantArray: Array<out TransitionConstant>
    val mBlendTreeConstantIndexArray: IntArray
    val mLeafInfoArray: Array<out LeafInfoConstant>
    val mBlendTreeConstantArray: Array<out BlendTreeConstant>
    val mNameID: UInt
    val mPathID: UInt
    val mFullPathID: UInt
    val mTagID: UInt
    val mSpeedParamID: UInt
    val mMirrorParamID: UInt
    val mCycleOffsetParamID: UInt
    val mTimeParamID: UInt
    val mSpeed: Float
    val mCycleOffset: Float
    val mIKOnFeet: Boolean
    val mWriteDefaultValues: Boolean
    val mLoop: Boolean
    val mMirror: Boolean
}

interface SelectorTransitionConstant {
    val mDestination: UInt
    val mConditionConstantArray: Array<out ConditionConstant>
}

interface SelectorStateConstant {
    val mTransitionConstantArray: Array<out SelectorTransitionConstant>
    val mFullPathID: UInt
    val mIsEntry: Boolean
}

interface StateMachineConstant {
    val mStateConstantArray: Array<out StateConstant>
    val mAnyStateTransitionConstantArray: Array<out TransitionConstant>
    val mSelectorStateConstantArray: Array<out SelectorStateConstant>
    val mDefaultState: UInt
    val mMotionSetCount: UInt
}

interface ValueArray {
    val mBoolValues: BooleanArray
    val mIntValues: IntArray
    val mFloatValues: FloatArray
    val mVectorValues: Array<out Vector4>
    val mPositionValues: Array<out Vector3>
    val mQuaternionValues: Array<out Vector4>
    val mScaleValues: Array<out Vector3>
}

interface ControllerConstant {
    val mLayerArray: Array<out LayerConstant>
    val mStateMachineArray: Array<out StateMachineConstant>
    val mValues: ValueArrayConstant
    val mDefaultValues: ValueArray
}
