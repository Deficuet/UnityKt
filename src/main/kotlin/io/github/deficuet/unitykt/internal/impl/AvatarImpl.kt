package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Vector
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class AvatarImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Avatar, AvatarFields(assetFile, info) {
    override val mAvatarSize: UInt get() {
        checkInitialize()
        return fmAvatarSize
    }
    override val mAvatar: AvatarConstant get() {
        checkInitialize()
        return fmAvatar
    }
    override val mTOS: Map<UInt, List<String>> get() {
        checkInitialize()
        return fmTOS
    }

    override fun read() {
        super.read()
        fmAvatarSize = reader.readUInt32()
        fmAvatar = AvatarConstantImpl(reader)
        fmTOS = reader.readArrayOf {
            with(reader) { readUInt32() to readAlignedString() }
        }.groupBy({ it.first }, { it.second })
    }
}

internal class NodeImpl(reader: ObjectReader): Node {
    override val mParentId = reader.readInt32()
    override val mAxesId = reader.readInt32()
}

internal class LimitImpl(reader: ObjectReader): Limit {
    override val mMin: Vector<*>
    override val mMax: Vector<*>

    init {
        if (reader.unityVersion >= intArrayOf(5, 4)) {
            mMin = reader.readVector3()
            mMax = reader.readVector3()
        } else {
            mMin = reader.readVector4()
            mMax = reader.readVector4()
        }
    }
}

internal class AxesImpl(reader: ObjectReader): Axes {
    override val mPreQ = reader.readVector4()
    override val mPostQ = reader.readVector4()
    override val mSgn: Vector<*>
    override val mLimit: Limit
    override val mLength: Float
    override val mType: UInt

    init {
        mSgn = if (reader.unityVersion >= intArrayOf(5, 4)) {
            reader.readVector3()
        } else reader.readVector4()
        mLimit = LimitImpl(reader)
        mLength = reader.readFloat()
        mType = reader.readUInt32()
    }
}

internal class SkeletonImpl(reader: ObjectReader): Skeleton {
    override val mNode = reader.readArrayOf { NodeImpl(this) }
    override val mID = reader.readUInt32Array()
    override val mAxesArray = reader.readArrayOf { AxesImpl(this) }
}

internal class SkeletonPoseImpl(reader: ObjectReader): SkeletonPose {
    override val mX = reader.readArrayOf { XFormImpl(this) }
}

internal class HandImpl(reader: ObjectReader): Hand {
    override val mHandBoneIndex = reader.readInt32Array()
}

internal class HandleImpl(reader: ObjectReader): Handle {
    override val mX = XFormImpl(reader)
    override val mParentHumanIndex = reader.readUInt32()
    override val mID = reader.readUInt32()
}

internal class ColliderImpl(reader: ObjectReader): Collider {
    override val mX = XFormImpl(reader)
    override val mType = reader.readUInt32()
    override val mXMotionType = reader.readUInt32()
    override val mYMotionType = reader.readUInt32()
    override val mZMotionType = reader.readUInt32()
    override val mMinLimitX = reader.readFloat()
    override val mMaxLimitX = reader.readFloat()
    override val mMaxLimitY = reader.readFloat()
    override val mMaxLimitZ = reader.readFloat()
}

internal class HumanImpl(reader: ObjectReader): Human {
    override val mRootX = XFormImpl(reader)
    override val mSkeleton = SkeletonImpl(reader)
    override val mSkeletonPose = SkeletonPoseImpl(reader)
    override val mLeftHand = HandImpl(reader)
    override val mRightHand = HandImpl(reader)
    override val mHandles: Array<HandleImpl>
    override val mColliderArray: Array<ColliderImpl>
    override val mHumanBoneIndex: IntArray
    override val mHumanBoneMass: FloatArray
    override val mColliderIndex: IntArray
    override val mScale: Float
    override val mAriTwist: Float
    override val mForeArmTwist: Float
    override val mUpperLegTwist: Float
    override val mLegTwist: Float
    override val mArmStretch: Float
    override val mLegStretch: Float
    override val mFeetSpacing: Float
    override val mHasLeftHand: Boolean
    override val mHasRightHand: Boolean
    override val mHasTDoF: Boolean

    init {
        val version = reader.unityVersion
        val v182 = intArrayOf(2018, 2)
        if (version < v182) {
            mHandles = reader.readArrayOf { HandleImpl(this) }
            mColliderArray = reader.readArrayOf { ColliderImpl(this) }
        } else {
            mHandles = emptyArray(); mColliderArray = emptyArray()
        }
        mHumanBoneIndex = reader.readInt32Array()
        mHumanBoneMass = reader.readFloatArray()
        mColliderIndex = if (version < v182) reader.readInt32Array() else IntArray(0)
        mScale = reader.readFloat()
        mAriTwist = reader.readFloat()
        mForeArmTwist = reader.readFloat()
        mUpperLegTwist = reader.readFloat()
        mLegTwist = reader.readFloat()
        mArmStretch = reader.readFloat()
        mLegStretch = reader.readFloat()
        mFeetSpacing = reader.readFloat()
        mHasLeftHand = reader.readBool()
        mHasRightHand = reader.readBool()
        mHasTDoF = if (version >= intArrayOf(5, 2)) reader.readBool() else false
        reader.alignStream()
    }
}

internal class AvatarConstantImpl(reader: ObjectReader): AvatarConstant {
    override val mAvatarSkeleton = SkeletonImpl(reader)
    override val mAvatarSkeletonPose = SkeletonPoseImpl(reader)
    override val mDefaultPose: SkeletonPose?
    override val mSkeletonNameIDArray: Array<UInt>
    override val mHuman: Human
    override val mHumanSkeletonIndexArray: IntArray
    override val mHumanSkeletonReverseIndexArray: IntArray
    override val mRootMotionBoneIndex: Int
    override val mRootMotionBoneX: XForm
    override val mRootMotionSkeleton: Skeleton?
    override val mRootMotionSkeletonPose: SkeletonPose?
    override val mRootMotionSkeletonIndexArray: IntArray

    init {
        val version = reader.unityVersion
        val v43 = intArrayOf(4, 3)
        if (version >= v43) {
            mDefaultPose = SkeletonPoseImpl(reader)
            mSkeletonNameIDArray = reader.readUInt32Array()
        } else {
            mDefaultPose = null; mSkeletonNameIDArray = emptyArray()
        }
        mHuman = HumanImpl(reader)
        mHumanSkeletonIndexArray = reader.readInt32Array()
        mHumanSkeletonReverseIndexArray = if (version >= v43) {
            reader.readInt32Array()
        } else IntArray(0)
        mRootMotionBoneIndex = reader.readInt32()
        mRootMotionBoneX = XFormImpl(reader)
        if (version >= v43) {
            mRootMotionSkeleton = SkeletonImpl(reader)
            mRootMotionSkeletonPose = SkeletonPoseImpl(reader)
            mRootMotionSkeletonIndexArray = reader.readInt32Array()
        } else {
            mRootMotionSkeleton = null
            mRootMotionSkeletonPose = null
            mRootMotionSkeletonIndexArray = IntArray(0)
        }
    }
}
