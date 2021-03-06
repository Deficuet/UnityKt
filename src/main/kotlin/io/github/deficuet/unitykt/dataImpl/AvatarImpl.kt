package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.math.Vector
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class AvatarImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mAvatarSize = reader.readUInt()
    val mAvatar = AvatarConstant(reader)
    val mTOS = reader.readArrayOf { with(reader) { readUInt() to readAlignedString() } }
}

class Node internal constructor(reader: ObjectReader) {
    val mParentId = reader.readInt()
    val mAxesId = reader.readInt()
}

class Limit internal constructor(reader: ObjectReader) {
    val mMin: Vector
    val mMax: Vector

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

class Axes internal constructor(reader: ObjectReader) {
    val mPreQ = reader.readVector4()
    val mPostQ = reader.readVector4()
    val mSgn: Vector
    val mLimit: Limit
    val mLength: Float
    val mType: UInt

    init {
        mSgn = if (reader.unityVersion >= intArrayOf(5, 4)) {
            reader.readVector3()
        } else reader.readVector4()
        mLimit = Limit(reader)
        mLength = reader.readFloat()
        mType = reader.readUInt()
    }
}

class Skeleton internal constructor(reader: ObjectReader) {
    val mNode = reader.readArrayOf { Node(reader) }
    val mID = reader.readNextUIntArray()
    val mAxesArray = reader.readArrayOf { Axes(reader) }
}

class SkeletonPose internal constructor(reader: ObjectReader) {
    val mX = reader.readArrayOf { XForm(reader) }
}

class Hand internal constructor(reader: ObjectReader) {
    val mHandBoneIndex = reader.readNextIntArray()
}

class Handle internal constructor(reader: ObjectReader) {
    val mX = XForm(reader)
    val mParentHumanIndex = reader.readUInt()
    val mID = reader.readUInt()
}

class Collider internal constructor(reader: ObjectReader) {
    val mX = XForm(reader)
    val mType = reader.readUInt()
    val mXMotionType = reader.readUInt()
    val mYMotionType = reader.readUInt()
    val mZMotionType = reader.readUInt()
    val mMinLimitX = reader.readFloat()
    val mMaxLimitX = reader.readFloat()
    val mMaxLimitY = reader.readFloat()
    val mMaxLimitZ = reader.readFloat()
}

class Human internal constructor(reader: ObjectReader) {
    val mRootX = XForm(reader)
    val mSkeleton = Skeleton(reader)
    val mSkeletonPose = SkeletonPose(reader)
    val mLeftHand = Hand(reader)
    val mRightHand = Hand(reader)
    val mHandles: Array<Handle>
    val mColliderArray: Array<Collider>
    val mHumanBoneIndex: IntArray
    val mHumanBoneMass: FloatArray
    val mColliderIndex: IntArray
    val mScale: Float
    val mAriTwist: Float
    val mForeArmTwist: Float
    val mUpperLegTwist: Float
    val mLegTwist: Float
    val mArmStretch: Float
    val mLegStretch: Float
    val mFeetSpacing: Float
    val mHasLeftHand: Boolean
    val mHasRightHand: Boolean
    val mHasTDoF: Boolean

    init {
        val version = reader.unityVersion
        val v182 = intArrayOf(2018, 2)
        if (version < v182) {
            mHandles = reader.readArrayOf { Handle(reader) }
            mColliderArray = reader.readArrayOf { Collider(reader) }
        } else {
            mHandles = emptyArray(); mColliderArray = emptyArray()
        }
        mHumanBoneIndex = reader.readNextIntArray()
        mHumanBoneMass = reader.readNextFloatArray()
        mColliderIndex = if (version < v182) reader.readNextIntArray() else intArrayOf()
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

class AvatarConstant internal constructor(reader: ObjectReader) {
    val mAvatarSkeleton = Skeleton(reader)
    val mAvatarSkeletonPose = SkeletonPose(reader)
    val mDefaultPose: SkeletonPose?
    val mSkeletonNameIDArray: Array<UInt>
    val mHuman: Human
    val mHumanSkeletonIndexArray: IntArray
    val mHumanSkeletonReverseIndexArray: IntArray
    val mRootMotionBoneIndex: Int
    val mRootMotionBoneX: XForm
    val mRootMotionSkeleton: Skeleton?
    val mRootMotionSkeletonPose: SkeletonPose?
    val mRootMotionSkeletonIndexArray: IntArray

    init {
        val version = reader.unityVersion
        val v43 = intArrayOf(4, 3)
        if (version >= v43) {
            mDefaultPose = SkeletonPose(reader)
            mSkeletonNameIDArray = reader.readNextUIntArray()
        } else {
            mDefaultPose = null; mSkeletonNameIDArray = emptyArray()
        }
        mHuman = Human(reader)
        mHumanSkeletonIndexArray = reader.readNextIntArray()
        mHumanSkeletonReverseIndexArray = if (version >= v43) {
            reader.readNextIntArray()
        } else intArrayOf()
        mRootMotionBoneIndex = reader.readInt()
        mRootMotionBoneX = XForm(reader)
        if (version >= v43) {
            mRootMotionSkeleton = Skeleton(reader)
            mRootMotionSkeletonPose = SkeletonPose(reader)
            mRootMotionSkeletonIndexArray = reader.readNextIntArray()
        } else {
            mRootMotionSkeleton = null
            mRootMotionSkeletonPose = null
            mRootMotionSkeletonIndexArray = intArrayOf()
        }
    }
}