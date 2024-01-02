package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Vector
import io.github.deficuet.unitykt.math.Vector4

interface Avatar: NamedObject {
    val mAvatarSize: UInt
    val mAvatar: AvatarConstant
    val mTOS: Map<UInt, List<String>>
}

interface Node {
    val mParentId: Int
    val mAxesId: Int
}

interface Limit {
    val mMin: Vector<*>
    val mMax: Vector<*>
}

interface Axes {
    val mPreQ: Vector4
    val mPostQ: Vector4
    val mSgn: Vector<*>
    val mLimit: Limit
    val mLength: Float
    val mType: UInt
}

interface Skeleton {
    val mNode: Array<out Node>
    val mID: Array<out UInt>
    val mAxesArray: Array<out Axes>
}

interface SkeletonPose {
    val mX: Array<out XForm>
}

interface Hand {
    val mHandBoneIndex: IntArray
}

interface Handle {
    val mX: XForm
    val mParentHumanIndex: UInt
    val mID: UInt
}

interface Collider {
    val mX: XForm
    val mType: UInt
    val mXMotionType: UInt
    val mYMotionType: UInt
    val mZMotionType: UInt
    val mMinLimitX: Float
    val mMaxLimitX: Float
    val mMaxLimitY: Float
    val mMaxLimitZ: Float
}

interface Human {
    val mRootX: XForm
    val mSkeleton: Skeleton
    val mSkeletonPose: SkeletonPose
    val mLeftHand: Hand
    val mRightHand: Hand
    val mHandles: Array<out Handle>
    val mColliderArray: Array<out Collider>
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
}

interface AvatarConstant {
    val mAvatarSkeleton: Skeleton
    val mAvatarSkeletonPose: SkeletonPose
    val mDefaultPose: SkeletonPose?
    val mSkeletonNameIDArray: Array<out UInt>
    val mHuman: Human
    val mHumanSkeletonIndexArray: IntArray
    val mHumanSkeletonReverseIndexArray: IntArray
    val mRootMotionBoneIndex: Int
    val mRootMotionBoneX: XForm
    val mRootMotionSkeleton: Skeleton?
    val mRootMotionSkeletonPose: SkeletonPose?
    val mRootMotionSkeletonIndexArray: IntArray
}
