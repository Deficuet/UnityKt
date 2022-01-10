package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class AnimatorController internal constructor(reader: ObjectReader): RuntimeAnimatorController(reader) {
    val mAnimationClips: Array<PPtr<AnimationClip>>

    init {
        reader += 4     //m_ControllerSize: UInt
        ControllerConstant(reader)  //m_Controller
        reader.readArrayOf { with(reader) { readUInt() to readAlignedString() } }  //m_TOS: Map<>
        mAnimationClips = reader.readArrayOf { PPtr(reader) }
    }
}

class HumanPoseMask internal constructor(reader: ObjectReader) {
    val word0 = reader.readUInt()
    val word1 = reader.readUInt()
    val word2 = if (reader.unityVersion >= intArrayOf(5, 2)) reader.readUInt() else 0u
}

class SkeletonMaskElement internal constructor(reader: ObjectReader) {
    val mPathHash = reader.readUInt()
    val mWeight = reader.readUInt()
}

class SkeletonMask internal constructor(reader: ObjectReader) {
    val mData = reader.readArrayOf { SkeletonMaskElement(reader) }
}

class LayerConstant internal constructor(reader: ObjectReader) {
    val mStateMachineIndex = reader.readUInt()
    val mStateMachineMotionSetIndex = reader.readUInt()
    val mBodyMask = HumanPoseMask(reader)
    val mSkeletonMask = SkeletonMask(reader)
    val mBinding = reader.readUInt()
    val mLayerBlendingMode = reader.readInt()
    val mDefaultWeight = if (reader.unityVersion >= intArrayOf(4, 2)) reader.readFloat() else 0f
    val mIKPass = reader.readBool()
    val mSyncedLayerAffectsTiming = if (reader.unityVersion >= intArrayOf(4, 2)) reader.readBool() else false

    init { reader.alignStream() }
}

class ConditionConstant internal constructor(reader: ObjectReader) {
    val mConditionMode = reader.readUInt()
    val mEventID = reader.readUInt()
    val mEventThreshold = reader.readFloat()
    val mExitTime = reader.readFloat()
}

class TransitionConstant internal constructor(reader: ObjectReader) {
    val mConditionConstantArray = reader.readArrayOf { ConditionConstant(reader) }
    val mDestinationState = reader.readUInt()
    val mFullPathID = if (reader.unityVersion[0] >= 5) reader.readUInt() else 0u
    val mID = reader.readUInt()
    val mUserID = reader.readUInt()
    val mTransitionDuration = reader.readFloat()
    val mTransitionOffset = reader.readFloat()
    val mExitTime: Float
    val mHasExitTime: Boolean
    val mHasFixedDuration: Boolean
    val mInterruptionSource: Int
    val mOrderedInterruption: Boolean
    val mAtomic: Boolean
    val mCanTransitionToSelf: Boolean

    init {
        val version = reader.unityVersion
        if (version[0] >= 5) {
            mExitTime = reader.readFloat()
            mHasExitTime = reader.readBool()
            mHasFixedDuration = reader.readBool()
            reader.alignStream()
            mInterruptionSource = reader.readInt()
            mOrderedInterruption = reader.readBool()
            mAtomic = false
        } else {
            mExitTime = 0f
            mHasExitTime = false
            mHasFixedDuration = false
            mInterruptionSource = 0
            mOrderedInterruption = false
            mAtomic = reader.readBool()
        }
        mCanTransitionToSelf = if (version >= intArrayOf(4, 5)) reader.readBool() else false
        reader.alignStream()
    }
}

class LeafInfoConstant internal constructor(reader: ObjectReader) {
    val mIDArray = reader.readNextUIntArray()
    val mIndexOffset = reader.readUInt()
}

class MotionNeighborList internal constructor(reader: ObjectReader) {
    val mNeighborArray = reader.readNextUIntArray()
}

class Blend2dDataConstant internal constructor(reader: ObjectReader) {
    val mChildPositionArray = reader.readNextVector2Array()
    val mChildMagnitudeArray = reader.readNextFloatArray()
    val mChildPairVectorArray = reader.readNextVector2Array()
    val mChildPairAvgMagInvArray = reader.readNextFloatArray()
    val mChildNeighborListArray = reader.readArrayOf { MotionNeighborList(reader) }
}

class Blend1dDataConstant internal constructor(reader: ObjectReader) {
    val mChildThresholdArray = reader.readNextFloatArray()
}

class BlendDirectDataConstant internal constructor(reader: ObjectReader) {
    val mChildBlendEventIDArray = reader.readNextUIntArray()
    val mNormalizedBlendValues = reader.readBool()

    init { reader.alignStream() }
}

class BlendTreeNodeConstant internal constructor(reader: ObjectReader) {
    val mBlendType: UInt
    val mBlendEventID: UInt
    val mBlendEventYID: UInt
    val mChildIndices: Array<UInt>
    val mChildThresholdArray: FloatArray
    val mBlend1dData: Blend1dDataConstant?
    val mBlend2dData: Blend2dDataConstant?
    val mBlendDirectData: BlendDirectDataConstant?
    val mClipID: UInt
    val mClipIndex: UInt
    val mDuration: Float
    val mCycleOffset: Float
    val mMirror: Boolean

    init {
        val version = reader.unityVersion
        val v41 = intArrayOf(4, 1)
        mBlendType = if (version >= v41) reader.readUInt() else 0u
        mBlendEventID = reader.readUInt()
        mBlendEventYID = if (version >= v41) reader.readUInt() else 0u
        mChildIndices = reader.readNextUIntArray()
        mChildThresholdArray = if (version < v41) reader.readNextFloatArray() else floatArrayOf()
        if (version >= v41) {
            mBlend1dData = Blend1dDataConstant(reader)
            mBlend2dData = Blend2dDataConstant(reader)
        } else {
            mBlend1dData = null; mBlend2dData = null
        }
        mBlendDirectData = if (version[0] >= 5) BlendDirectDataConstant(reader) else null
        mClipID = reader.readUInt()
        mClipIndex = if (intArrayOf(4, 5) <= version && version[0] < 5) reader.readUInt() else 0u
        mDuration = reader.readFloat()
        if (version >= intArrayOf(4, 1, 3)) {
            mCycleOffset = reader.readFloat()
            mMirror = reader.readBool()
            reader.alignStream()
        } else {
            mCycleOffset = 0f; mMirror = false
        }
    }
}

class BlendTreeConstant internal constructor(reader: ObjectReader) {
    val mNodeArray = reader.readArrayOf { BlendTreeNodeConstant(reader) }
    val mBlendEventArrayConstant = if (reader.unityVersion < intArrayOf(4, 5)) {
        ValueArrayConstant(reader)
    } else null
}

class StateConstant internal constructor(reader: ObjectReader) {
    val mTransitionConstantArray = reader.readArrayOf { TransitionConstant(reader) }
    val mBlendTreeConstantIndexArray = reader.readNextIntArray()
    val mLeafInfoArray = if (reader.unityVersion < intArrayOf(5, 2)) {
        reader.readArrayOf { LeafInfoConstant(reader) }
    } else emptyArray()
    val mBlendTreeConstantArray = reader.readArrayOf { BlendTreeConstant(reader) }
    val mNameID = reader.readUInt()
    val mPathID = if (reader.unityVersion >= intArrayOf(4, 3)) reader.readUInt() else 0u
    val mFullPathID = if (reader.unityVersion[0] >= 5) reader.readUInt() else 0u
    val mTagID = reader.readUInt()
    val mSpeedParamID: UInt
    val mMirrorParamID: UInt
    val mCycleOffsetParamID: UInt
    val mSpeed: Float
    val mCycleOffset: Float
    val mIKOnFeet: Boolean
    val mWriteDefaultValues: Boolean
    val mLoop: Boolean
    val mMirror: Boolean

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(5, 1)) {
            mSpeedParamID = reader.readUInt()
            mMirrorParamID = reader.readUInt()
            mCycleOffsetParamID = reader.readUInt()
        } else {
            mSpeedParamID = 0u
            mMirrorParamID = 0u
            mCycleOffsetParamID = 0u
        }
        if (version >= intArrayOf(2017, 2)) reader += 4     //m_TimeParamID: UInt
        mSpeed = reader.readFloat()
        mCycleOffset = if (version >= intArrayOf(4, 1)) reader.readFloat() else 0f
        mIKOnFeet = reader.readBool()
        mWriteDefaultValues = if (version[0] >= 5) reader.readBool() else false
        mLoop = reader.readBool()
        mMirror = if (version >= intArrayOf(4, 1)) reader.readBool() else false
        reader.alignStream()
    }
}

class SelectorTransitionConstant internal constructor(reader: ObjectReader) {
    val mDestination = reader.readUInt()
    val mConditionConstantArray = reader.readArrayOf { ConditionConstant(reader) }
}

class SelectorStateConstant internal constructor(reader: ObjectReader) {
    val mTransitionConstantArray = reader.readArrayOf { SelectorTransitionConstant(reader) }
    val mFullPathID = reader.readUInt()
    val mIsEntry = reader.readBool()

    init { reader.alignStream() }
}

class StateMachineConstant internal constructor(reader: ObjectReader) {
    val mStateConstantArray = reader.readArrayOf { StateConstant(reader) }
    val mAnyStateTransitionConstantArray = reader.readArrayOf { TransitionConstant(reader) }
    val mSelectorStateConstantArray = if (reader.unityVersion[0] >= 5) {
        reader.readArrayOf { SelectorStateConstant(reader) }
    } else emptyArray()
    val mDefaultState = reader.readUInt()
    val mMotionSetCount = reader.readUInt()
}

class ValueArray internal constructor(reader: ObjectReader) {
    val mBoolValues: BooleanArray
    val mIntValues: IntArray
    val mFloatValues: FloatArray
    val mVectorValues: Array<Vector4>
    val mPositionValues: Array<Vector3>
    val mQuaternionValues: Array<Vector4>
    val mScaleValues: Array<Vector3>

    init {
        val version = reader.unityVersion
        val v55 = intArrayOf(5, 5); val v54 = intArrayOf(5, 4)
        var bool: BooleanArray = booleanArrayOf()
        var ints: IntArray = intArrayOf()
        var floats: FloatArray = floatArrayOf()
        if (version < v55) {
            bool = reader.readNextBoolArray()
            reader.alignStream()
            ints = reader.readNextIntArray()
            floats = reader.readNextFloatArray()
        }
        if (version < intArrayOf(4, 3)) {
            mVectorValues = reader.readNextVector4Array()
            mPositionValues = emptyArray()
            mQuaternionValues = emptyArray()
            mScaleValues = emptyArray()
        } else {
            mVectorValues = emptyArray()
            mPositionValues = reader.readArrayOf {
                if (version >= v54) reader.readVector3()
                else reader.readVector4().vector3
            }
            mQuaternionValues = reader.readNextVector4Array()
            mScaleValues = reader.readArrayOf {
                if (version >= v54) reader.readVector3()
                else reader.readVector4().vector3
            }
            if (version >= v55) {
                floats = reader.readNextFloatArray()
                ints = reader.readNextIntArray()
                bool = reader.readNextBoolArray()
                reader.alignStream()
            }
        }
        mBoolValues = bool
        mIntValues = ints
        mFloatValues = floats
    }
}

class ControllerConstant internal constructor(reader: ObjectReader) {
    val mLayerArray = reader.readArrayOf { LayerConstant(reader) }
    val mStateMachineArray = reader.readArrayOf { StateMachineConstant(reader) }
    val mValues = ValueArrayConstant(reader)
    val mDefaultValues = ValueArray(reader)
}