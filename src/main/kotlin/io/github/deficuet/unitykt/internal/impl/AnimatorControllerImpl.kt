package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class AnimatorControllerImpl(
    assetFile: SerializedFile, info: ObjectInfo
): AnimatorController, AnimatorControllerFields(assetFile, info) {
    override val mControllerSize: UInt get() {
        checkInitialize()
        return fmControllerSize
    }
    override val mController: ControllerConstant get() {
        checkInitialize()
        return fmController
    }
    override val mTOS: Map<UInt, List<String>> get() {
        checkInitialize()
        return fmTOS
    }
    override val mAnimationClip: Array<out PPtr<AnimationClip>> get() {
        checkInitialize()
        return fmAnimationClip
    }

    override fun read() {
        super.read()
        fmControllerSize = reader.readUInt32()
        fmController = ControllerConstantImpl(reader)
        fmTOS = reader.readArrayOf { readUInt32() to readAlignedString() }
            .groupBy({ it.first }, { it.second })
        fmAnimationClip = reader.readArrayOf { PPtrImpl(this) }
    }
}

internal class HumanPoseMaskImpl(reader: ObjectReader): HumanPoseMask {
    override val word0 = reader.readUInt32()
    override val word1 = reader.readUInt32()
    override val word2 = if (reader.unityVersion >= intArrayOf(5, 2)) reader.readUInt32() else 0u
}

internal class SkeletonMaskElementImpl(reader: ObjectReader): SkeletonMaskElement {
    override val mPathHash = reader.readUInt32()
    override val mWeight = reader.readFloat()
}

internal class SkeletonMaskImpl(reader: ObjectReader): SkeletonMask {
    override val mData = reader.readArrayOf { SkeletonMaskElementImpl(this) }
}

internal class LayerConstantImpl(reader: ObjectReader): LayerConstant {
    override val mStateMachineIndex = reader.readUInt32()
    override val mStateMachineMotionSetIndex = reader.readUInt32()
    override val mBodyMask = HumanPoseMaskImpl(reader)
    override val mSkeletonMask = SkeletonMaskImpl(reader)
    override val mBinding = reader.readUInt32()
    override val mLayerBlendingMode = reader.readInt32()
    override val mDefaultWeight = if (reader.unityVersion >= intArrayOf(4, 2)) reader.readFloat() else 0f
    override val mIKPass = reader.readBool()
    override val mSyncedLayerAffectsTiming = if (reader.unityVersion >= intArrayOf(4, 2)) reader.readBool() else false

    init { reader.alignStream() }
}

internal class ConditionConstantImpl(reader: ObjectReader): ConditionConstant {
    override val mConditionMode = reader.readUInt32()
    override val mEventID = reader.readUInt32()
    override val mEventThreshold = reader.readFloat()
    override val mExitTime = reader.readFloat()
}

internal class TransitionConstantImpl(reader: ObjectReader): TransitionConstant {
    override val mConditionConstantArray = reader.readArrayOf { ConditionConstantImpl(this) }
    override val mDestinationState = reader.readUInt32()
    override val mFullPathID = if (reader.unityVersion[0] >= 5) reader.readUInt32() else 0u
    override val mID = reader.readUInt32()
    override val mUserID = reader.readUInt32()
    override val mTransitionDuration = reader.readFloat()
    override val mTransitionOffset = reader.readFloat()
    override val mExitTime: Float
    override val mHasExitTime: Boolean
    override val mHasFixedDuration: Boolean
    override val mInterruptionSource: Int
    override val mOrderedInterruption: Boolean
    override val mAtomic: Boolean
    override val mCanTransitionToSelf: Boolean

    init {
        val version = reader.unityVersion
        if (version[0] >= 5) {
            mExitTime = reader.readFloat()
            mHasExitTime = reader.readBool()
            mHasFixedDuration = reader.readBool()
            reader.alignStream()
            mInterruptionSource = reader.readInt32()
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

internal class LeafInfoConstantImpl(reader: ObjectReader): LeafInfoConstant {
    override val mIDArray = reader.readUInt32Array()
    override val mIndexOffset = reader.readUInt32()
}

internal class MotionNeighborListImpl(reader: ObjectReader): MotionNeighborList {
    override val mNeighborArray = reader.readUInt32Array()
}

internal class Blend2dDataConstantImpl(reader: ObjectReader): Blend2dDataConstant {
    override val mChildPositionArray = reader.readVector2Array()
    override val mChildMagnitudeArray = reader.readFloatArray()
    override val mChildPairVectorArray = reader.readVector2Array()
    override val mChildPairAvgMagInvArray = reader.readFloatArray()
    override val mChildNeighborListArray = reader.readArrayOf { MotionNeighborListImpl(this) }
}

internal class Blend1dDataConstantImpl(reader: ObjectReader): Blend1dDataConstant {
    override val mChildThresholdArray = reader.readFloatArray()
}

internal class BlendDirectDataConstantImpl(reader: ObjectReader): BlendDirectDataConstant {
    override val mChildBlendEventIDArray = reader.readUInt32Array()
    override val mNormalizedBlendValues = reader.readBool()

    init { reader.alignStream() }
}

internal class BlendTreeNodeConstantImpl(reader: ObjectReader): BlendTreeNodeConstant {
    override val mBlendType: UInt
    override val mBlendEventID: UInt
    override val mBlendEventYID: UInt
    override val mChildIndices: Array<UInt>
    override val mChildThresholdArray: FloatArray
    override val mBlend1dData: Blend1dDataConstant?
    override val mBlend2dData: Blend2dDataConstant?
    override val mBlendDirectData: BlendDirectDataConstant?
    override val mClipID: UInt
    override val mClipIndex: UInt
    override val mDuration: Float
    override val mCycleOffset: Float
    override val mMirror: Boolean

    init {
        val version = reader.unityVersion
        val v41 = intArrayOf(4, 1)
        mBlendType = if (version >= v41) reader.readUInt32() else 0u
        mBlendEventID = reader.readUInt32()
        mBlendEventYID = if (version >= v41) reader.readUInt32() else 0u
        mChildIndices = reader.readUInt32Array()
        mChildThresholdArray = if (version < v41) reader.readFloatArray() else FloatArray(0)
        if (version >= v41) {
            mBlend1dData = Blend1dDataConstantImpl(reader)
            mBlend2dData = Blend2dDataConstantImpl(reader)
        } else {
            mBlend1dData = null; mBlend2dData = null
        }
        mBlendDirectData = if (version[0] >= 5) BlendDirectDataConstantImpl(reader) else null
        mClipID = reader.readUInt32()
        mClipIndex = if (version[0] == 4 && version[1] >= 5) reader.readUInt32() else 0u
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

internal class BlendTreeConstantImpl(reader: ObjectReader): BlendTreeConstant {
    override val mNodeArray = reader.readArrayOf { BlendTreeNodeConstantImpl(this) }
    override val mBlendEventArrayConstant = if (reader.unityVersion < intArrayOf(4, 5)) {
        ValueArrayConstantImpl(reader)
    } else null
}

internal class StateConstantImpl(reader: ObjectReader): StateConstant {
    override val mTransitionConstantArray = reader.readArrayOf { TransitionConstantImpl(this) }
    override val mBlendTreeConstantIndexArray = reader.readInt32Array()
    override val mLeafInfoArray = if (reader.unityVersion < intArrayOf(5, 2)) {
        reader.readArrayOf { LeafInfoConstantImpl(this) }
    } else emptyArray()
    override val mBlendTreeConstantArray = reader.readArrayOf { BlendTreeConstantImpl(this) }
    override val mNameID = reader.readUInt32()
    override val mPathID = if (reader.unityVersion >= intArrayOf(4, 3)) reader.readUInt32() else 0u
    override val mFullPathID = if (reader.unityVersion[0] >= 5) reader.readUInt32() else 0u
    override val mTagID = reader.readUInt32()
    override val mSpeedParamID: UInt
    override val mMirrorParamID: UInt
    override val mCycleOffsetParamID: UInt
    override val mTimeParamID: UInt
    override val mSpeed: Float
    override val mCycleOffset: Float
    override val mIKOnFeet: Boolean
    override val mWriteDefaultValues: Boolean
    override val mLoop: Boolean
    override val mMirror: Boolean

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(5, 1)) {
            mSpeedParamID = reader.readUInt32()
            mMirrorParamID = reader.readUInt32()
            mCycleOffsetParamID = reader.readUInt32()
        } else {
            mSpeedParamID = 0u
            mMirrorParamID = 0u
            mCycleOffsetParamID = 0u
        }
        mTimeParamID = if (version >= intArrayOf(2017, 2)) reader.readUInt32() else 0u
        mSpeed = reader.readFloat()
        mCycleOffset = if (version >= intArrayOf(4, 1)) reader.readFloat() else 0f
        mIKOnFeet = reader.readBool()
        mWriteDefaultValues = if (version[0] >= 5) reader.readBool() else false
        mLoop = reader.readBool()
        mMirror = if (version >= intArrayOf(4, 1)) reader.readBool() else false
        reader.alignStream()
    }
}

internal class SelectorTransitionConstantImpl(reader: ObjectReader): SelectorTransitionConstant {
    override val mDestination = reader.readUInt32()
    override val mConditionConstantArray = reader.readArrayOf { ConditionConstantImpl(this) }
}

internal class SelectorStateConstantImpl(reader: ObjectReader): SelectorStateConstant {
    override val mTransitionConstantArray = reader.readArrayOf { SelectorTransitionConstantImpl(this) }
    override val mFullPathID = reader.readUInt32()
    override val mIsEntry = reader.readBool()

    init { reader.alignStream() }
}

internal class StateMachineConstantImpl(reader: ObjectReader): StateMachineConstant {
    override val mStateConstantArray = reader.readArrayOf { StateConstantImpl(this) }
    override val mAnyStateTransitionConstantArray = reader.readArrayOf { TransitionConstantImpl(this) }
    override val mSelectorStateConstantArray = if (reader.unityVersion[0] >= 5) {
        reader.readArrayOf { SelectorStateConstantImpl(this) }
    } else emptyArray()
    override val mDefaultState = reader.readUInt32()
    override val mMotionSetCount = reader.readUInt32()
}

internal class ValueArrayImpl(reader: ObjectReader): ValueArray {
    override val mBoolValues: BooleanArray
    override val mIntValues: IntArray
    override val mFloatValues: FloatArray
    override val mVectorValues: Array<Vector4>
    override val mPositionValues: Array<Vector3>
    override val mQuaternionValues: Array<Vector4>
    override val mScaleValues: Array<Vector3>

    init {
        val version = reader.unityVersion
        val v55 = intArrayOf(5, 5); val v54 = intArrayOf(5, 4)
        var bool = BooleanArray(0)
        var ints = IntArray(0)
        var floats = FloatArray(0)
        if (version < v55) {
            bool = reader.readBoolArray()
            reader.alignStream()
            ints = reader.readInt32Array()
            floats = reader.readFloatArray()
        }
        if (version < intArrayOf(4, 3)) {
            mVectorValues = reader.readVector4Array()
            mPositionValues = emptyArray()
            mQuaternionValues = emptyArray()
            mScaleValues = emptyArray()
        } else {
            mVectorValues = emptyArray()
            mPositionValues = reader.readArrayOf {
                if (version >= v54) reader.readVector3()
                else reader.readVector4().vector3
            }
            mQuaternionValues = reader.readVector4Array()
            mScaleValues = reader.readArrayOf {
                if (version >= v54) reader.readVector3()
                else reader.readVector4().vector3
            }
            if (version >= v55) {
                floats = reader.readFloatArray()
                ints = reader.readInt32Array()
                bool = reader.readBoolArray()
                reader.alignStream()
            }
        }
        mBoolValues = bool
        mIntValues = ints
        mFloatValues = floats
    }
}

internal class ControllerConstantImpl(reader: ObjectReader): ControllerConstant {
    override val mLayerArray = reader.readArrayOf { LayerConstantImpl(this) }
    override val mStateMachineArray = reader.readArrayOf { StateMachineConstantImpl(this) }
    override val mValues = ValueArrayConstantImpl(reader)
    override val mDefaultValues = ValueArrayImpl(reader)
}
