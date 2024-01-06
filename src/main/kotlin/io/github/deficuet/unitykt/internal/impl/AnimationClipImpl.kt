package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Quaternion
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.util.*
import kotlin.math.sqrt

internal class AnimationClipImpl(
    assetFile: SerializedFile, info: ObjectInfo
): AnimationClip, AnimationClipFields(assetFile, info) {
    override val mAnimationType: AnimationType get() {
        checkInitialize()
        return fmAnimationType
    }
    override val mLegacy: Boolean get() {
        checkInitialize()
        return fmLegacy
    }
    override val mCompressed: Boolean get() {
        checkInitialize()
        return fmCompressed
    }
    override val mUseHighQualityCurve: Boolean get() {
        checkInitialize()
        return fmUseHighQualityCurve
    }
    override val mRotationCurves: Array<QuaternionCurveImpl> get() {
        checkInitialize()
        return fmRotationCurves
    }
    override val mCompressedRotationCurves: Array<CompressedAnimationCurveImpl> get() {
        checkInitialize()
        return fmCompressedRotationCurves
    }
    override val mEulerCurves: Array<Vector3CurveImpl> get() {
        checkInitialize()
        return fmEulerCurves
    }
    override val mPositionCurves: Array<Vector3CurveImpl> get() {
        checkInitialize()
        return fmPositionCurves
    }
    override val mScaleCurves: Array<Vector3CurveImpl> get() {
        checkInitialize()
        return fmScaleCurves
    }
    override val mFloatCurves: Array<FloatCurveImpl> get() {
        checkInitialize()
        return fmFloatCurves
    }
    override val mPPtrCurves: Array<PPtrCurveImpl> get() {
        checkInitialize()
        return fmPPtrCurves
    }
    override val mSampleRate: Float get() {
        checkInitialize()
        return fmSampleRate
    }
    override val mWrapMode: Int get() {
        checkInitialize()
        return fmWrapMode
    }
    override val mBounds: AABBImpl? get() {
        checkInitialize()
        return fmBounds
    }
    override val mMuscleClipSize: UInt get() {
        checkInitialize()
        return fmMuscleClipSize
    }
    override val mMuscleClip: ClipMuscleConstantImpl? get() {
        checkInitialize()
        return fmMuscleClip
    }
    override val mClipBindingConstant: AnimationClipBindingConstantImpl? get() {
        checkInitialize()
        return fmClipBindingConstant
    }
    override val mEvents: Array<AnimationEventImpl> get() {
        checkInitialize()
        return fmEvents
    }

    override fun read() {
        super.read()
        val v43 = intArrayOf(4, 3)
        fmLegacy = if (unityVersion[0] >= 5) {
            fmAnimationType = AnimationType.Default
            reader.readBool()
        } else if (unityVersion[0] >= 4) {
            fmAnimationType = AnimationType.of(reader.readInt32())
            fmAnimationType == AnimationType.Legacy
        } else {
            fmAnimationType = AnimationType.Default
            true
        }
        fmCompressed = reader.readBool()
        fmUseHighQualityCurve = if (unityVersion >= v43) reader.readBool() else false
        reader.alignStream()
        fmRotationCurves = reader.readArrayOf { QuaternionCurveImpl(this) }
        fmCompressedRotationCurves = reader.readArrayOf { CompressedAnimationCurveImpl(this) }
        fmEulerCurves = if (unityVersion >= intArrayOf(5, 3)) {
            reader.readArrayOf { Vector3CurveImpl(this) }
        } else emptyArray()
        fmPositionCurves = reader.readArrayOf { Vector3CurveImpl(this) }
        fmScaleCurves = reader.readArrayOf { Vector3CurveImpl(this) }
        fmFloatCurves = reader.readArrayOf { FloatCurveImpl(this) }
        fmPPtrCurves = if (unityVersion >= v43) {
            reader.readArrayOf { PPtrCurveImpl(this) }
        } else emptyArray()
        fmSampleRate = reader.readFloat()
        fmWrapMode = reader.readInt32()
        fmBounds = if (unityVersion >= intArrayOf(3, 4)) AABBImpl(reader) else null
        if (unityVersion[0] >= 4) {
            fmMuscleClipSize = reader.readUInt32()
            fmMuscleClip = ClipMuscleConstantImpl(reader)
        } else {
            fmMuscleClipSize = 0u; fmMuscleClip = null
        }
        fmClipBindingConstant = if (unityVersion >= v43) AnimationClipBindingConstantImpl(reader) else null
        if (unityVersion >= intArrayOf(2018, 3)) {
            reader.skip(2)     //m_HasGenericRootTransform, m_HasMotionFloatCurves: Boolean
            reader.alignStream()
        }
        fmEvents = reader.readArrayOf { AnimationEventImpl(this) }
        if (unityVersion[0] >= 2017) reader.alignStream()
    }
}

internal class KeyFrameImpl<out T>(reader: ObjectReader, readerFunc: () -> T): KeyFrame<T> {
    override val time = reader.readFloat()
    override val value = readerFunc()
    override val inSlope = readerFunc()
    override val outSlope = readerFunc()
    override val weightedMode: Int
    override val inWeight: T?
    override val outWeight: T?

    init {
        if (reader.unityVersion[0] >= 2018) {
            weightedMode = reader.readInt32()
            inWeight = readerFunc()
            outWeight = readerFunc()
        } else {
            weightedMode = 0; inWeight = null; outWeight = null
        }
    }
}

internal class AnimationCurveImpl<out T>(reader: ObjectReader, readerFunc: () -> T): AnimationCurve<T> {
    override val mCurve: Array<out KeyFrameImpl<T>> = reader.readArrayOf { KeyFrameImpl(this, readerFunc) }
    override val mPreInfinity = reader.readInt32()
    override val mPostInfinity = reader.readInt32()
    override val mRotationOrder = if (reader.unityVersion >= intArrayOf(5, 3)) reader.readInt32() else 0
}

internal class QuaternionCurveImpl(reader: ObjectReader): QuaternionCurve {
    override val curve = AnimationCurveImpl(reader, reader::readQuaternion)
    override val path = reader.readAlignedString()
}

internal class PackedFloatVectorImpl(reader: ObjectReader): PackedFloatVector {
    override val mNumItems = reader.readUInt32()
    override val mRange = reader.readFloat()
    override val mStart = reader.readFloat()
    override val mData: ByteArray
    override val mBitSize: UByte

    init {
        mData = reader.read(reader.readInt32())
        reader.alignStream()
        mBitSize = reader.readUInt8()
        reader.alignStream()
    }

    override fun unpackFloats(itemCountInChunk: Int, chunkStride: Int, start: Int, chunkCount: Int): FloatArray {
        val bitSize = mBitSize.toInt()
        var bitPos = start * bitSize
        var indexPos = (bitPos / 8).toUInt()
        bitPos %= 8
        val scale = 1.0f / mRange
        val numChunks = if (chunkCount == -1) {
            mNumItems.toInt() / itemCountInChunk
        } else chunkCount
        val end = chunkStride * numChunks / 4
        val data = mutableListOf<Float>()
        var idx = 0
        while (idx != end) {
            for (i in 0 until itemCountInChunk) {
                var x = 0u
                var bits = 0
                while (bits < bitSize) {
                    x = x.or(mData[indexPos].shr(bitPos).shl(bits).toUInt())
                    val num = minOf(bitSize - bits, 8 - bitPos)
                    bitPos += num; bits += num
                    if (bitPos == 8) {
                        indexPos++; bitPos = 0
                    }
                }
                x = x.and(1u.shl(bitSize) - 1u)
                data.add(x.toFloat() / (scale * (1.shl(bitSize) - 1)) + mStart)
            }
            idx += chunkStride / 4
        }
        return data.toFloatArray()
    }
}

internal class PackedIntVectorImpl: PackedIntVector {
    override var mNumItems: UInt
    override val mData: ByteArray
    override var mBitSize: UByte

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(reader: ObjectReader) {
        mNumItems = reader.readUInt32()
        mData = reader.read(reader.readInt32())
        reader.alignStream()
        mBitSize = reader.readUInt8()
        reader.alignStream()
    }

    override fun unpackInts(): IntArray {
        val data = IntArray(mNumItems.toInt())
        var indexPos = 0u; var bitPos = 0
        val bitSize = mBitSize.toInt()
        for (i in 0 until mNumItems.toInt()) {
            var bits = 0; var value = 0
            while (bits < bitSize) {
                value = value.or(mData[indexPos].shr(bitPos).shl(bits))
                val num = minOf(bitSize - bits, 8 - bitPos)
                bitPos += num; bits += num
                if (bitPos == 8) {
                    indexPos++; bitPos = 0
                }
            }
            data[i] = value.and(1.shl(bitSize) - 1)
        }
        return data
    }
}

internal class PackedQuatVectorImpl(reader: ObjectReader): PackedQuatVector {
    override val mNumItems = reader.readUInt32()
    override val mData: ByteArray

    init {
        mData = reader.read(reader.readInt32())
        reader.alignStream()
    }

    override fun unpackQuats(): Array<out Quaternion> {
        val data = Array(mNumItems.toInt()) { Quaternion.Zero }
        var indexPos = 0u; var bitPos = 0
        for (i in 0 until mNumItems.toInt()) {
            var flags = 0u; var bits = 0
            while (bits < 3) {
                flags = flags.or((mData[indexPos].shr(bitPos).shl(bits)).toUInt())
                val num = minOf(3 - bits, 8 - bitPos)
                bitPos += num; bits += num
                if (bitPos == 8) {
                    indexPos++; bitPos = 0
                }
            }
            flags = flags.and(7u)
            val qFloats = FloatArray(4)
            var sum = 0f
            for (j in 0u..3u) {
                if ((flags.and(3u)) != j) {
                    val bitSize = if (((flags.and(3u)) + 1u) % 4u == j) 9 else 10
                    var x = 0u; bits = 0
                    while (bits < bitSize) {
                        x = x.or(((mData[indexPos].shr(bitPos)).shl(bits)).toUInt())
                        val num = minOf(bitSize - bits, 8 - bitPos)
                        bitPos += num; bits += num
                        if (bitPos == 8) {
                            indexPos++; bitPos = 0
                        }
                    }
                    x = x.and(1u.shl(bitSize) - 1u)
                    val f = x.toFloat() / (0.5f * (1.shl(bitSize) - 1)) - 1
                    sum += f * f
                    qFloats[j.toInt()] = f
                }
            }
            val qLast = (flags.and(3u)).toInt()
            qFloats[qLast] = sqrt(1 - sum)
            if ((flags.and(4u)) != 0u) qFloats[qLast] = -qFloats[qLast]
            data[i] = Quaternion(qFloats[0], qFloats[1], qFloats[2], qFloats[3])
        }
        return data
    }
}

internal class CompressedAnimationCurveImpl(reader: ObjectReader): CompressedAnimationCurve {
    override val mPath = reader.readAlignedString()
    override val mTimes = PackedIntVectorImpl(reader)
    override val mValues = PackedQuatVectorImpl(reader)
    override val mSlopes = PackedFloatVectorImpl(reader)
    override val mPreInfinity = reader.readInt32()
    override val mPostInfinity = reader.readInt32()
}

internal class Vector3CurveImpl(reader: ObjectReader): Vector3Curve {
    override val curve = AnimationCurveImpl(reader, reader::readVector3)
    override val path = reader.readAlignedString()
}

internal class FloatCurveImpl(reader: ObjectReader): FloatCurve {
    override val curve = AnimationCurveImpl(reader, reader::readFloat)
    override val attribute = reader.readAlignedString()
    override val path = reader.readAlignedString()
    override val classID: ClassIDType
    override val script: PPtr<MonoScript>

    init {
        val id = reader.readInt32()
        classID = ClassIDType.of(id)
        script = PPtrImpl(reader)
    }
}

internal class PPtrKeyFrameImpl(reader: ObjectReader): PPtrKeyFrame {
    override val time = reader.readFloat()
    override val value = PPtrImpl<UnityObject>(reader)
}

internal class PPtrCurveImpl(reader: ObjectReader): PPtrCurve {
    override val curve = reader.readArrayOf { PPtrKeyFrameImpl(this) }
    override val attribute = reader.readAlignedString()
    override val path = reader.readAlignedString()
    override val classID = reader.readInt32()
    override val script = PPtrImpl<MonoScript>(reader)
}

internal class AABBImpl(reader: ObjectReader): AABB {
    override val mCenter = reader.readVector3()
    override val mExtent = reader.readVector3()
}

internal class XFormImpl(reader: ObjectReader): XForm {
    override val t: Vector3
    override val q: Quaternion
    override val s: Vector3

    init {
        val version = reader.unityVersion
        val v = intArrayOf(5, 4)
        t = if (version >= v) reader.readVector3() else reader.readVector4().vector3
        q = reader.readQuaternion()
        s = if (version >= v) reader.readVector3() else reader.readVector4().vector3
    }
}

internal class HandPoseImpl(reader: ObjectReader): HandPose {
    override val mGrabX = XFormImpl(reader)
    override val mDoFArray = reader.readFloatArray()
    override val mOverride = reader.readFloat()
    override val mCloseOpen = reader.readFloat()
    override val mInOut = reader.readFloat()
    override val mGrab = reader.readFloat()
}

internal class HumanGoalImpl(reader: ObjectReader): HumanGoal {
    override val mX = XFormImpl(reader)
    override val mWeightT = reader.readFloat()
    override val mWeightR = reader.readFloat()
    override val mHintT: Vector3
    override val mHintWeightT: Float

    init {
        val version = reader.unityVersion
        if (version[0] > 5) {
            mHintT = if (version >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
            mHintWeightT = reader.readFloat()
        } else {
            mHintT = Vector3.Zero
            mHintWeightT = 0f
        }
    }
}

internal class HumanPoseImpl(reader: ObjectReader): HumanPose {
    override val mRootX = XFormImpl(reader)
    override val mLookAt = if (reader.unityVersion >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
    override val mLookAtWeight = reader.readVector4()
    override val mGoalArray = reader.readArrayOf { HumanGoalImpl(this) }
    override val mLeftHandPose = HandPoseImpl(reader)
    override val mRightHandPose = HandPoseImpl(reader)
    override val mDoFArray = reader.readFloatArray()
    override val mTDoFArray = if (reader.unityVersion >= intArrayOf(5, 2)) {
        reader.readArrayOf {
            if (unityVersion >= intArrayOf(5, 4)) readVector3() else readVector4().vector3
        }
    } else emptyArray()
}

internal class StreamedCurveKeyImpl(reader: EndianBinaryReader): StreamedCurveKey {
    override val index = reader.readInt32()
    override val coeff = reader.readFloatArray(4)
    override val outSlope = coeff[2]
    override val value = coeff[3]
    override var inSlope = 0f

    override fun nextInSlope(deltaX: Float, rhs: StreamedCurveKey): Float {
        if (coeff[0] == 0f && coeff[1] == 0f && coeff[2] == 0f) return Float.POSITIVE_INFINITY
        val dx = maxOf(deltaX, 0.0001f)
        val dy = rhs.value - value
        val length = 1f / dx / dx
        val d1 = outSlope * dx
        val d2 = dy * 3 - d1 * 2 - coeff[1] / length
        return d2 / dx
    }
}

internal class StreamedFrameImpl(reader: EndianBinaryReader): StreamedFrame {
    override val time = reader.readFloat()
    override val keyList = reader.readArrayOf { StreamedCurveKeyImpl(this) }
}

internal class StreamedClipImpl(reader: ObjectReader): StreamedClip {
    override val data = reader.readUInt32Array()
    override val curveCount = reader.readUInt32()

    override fun readData(): List<StreamedFrame> {
        val frameList = mutableListOf<StreamedFrameImpl>()
        val buffer = ByteArray(data.size * 4) {
            data[it / 4].shr(24 - (it % 4) * 8).toByte()
        }
        EndianByteArrayReader(buffer).use {
            while (it.position < it.length) {
                frameList.add(StreamedFrameImpl(it))
            }
        }
        for (frameIdx in 2 until frameList.size) {
            val frame = frameList[frameIdx]
            for (curveKey in frame.keyList) {
                for (i in frameIdx - 1 downTo 0) {
                    val preFrame = frameList[i]
                    val preCurveKey = preFrame.keyList.firstOrNull { it.index == curveKey.index }
                    if (preCurveKey != null) {
                        curveKey.inSlope = preCurveKey.nextInSlope(frame.time - preFrame.time, curveKey)
                        break
                    }
                }
            }
        }
        return frameList
    }
}

internal class DenseClipImpl(reader: ObjectReader): DenseClip {
    override val mFrameCount = reader.readInt32()
    override val mCurveCount = reader.readUInt32()
    override val mSampleRate = reader.readFloat()
    override val mBeginTime = reader.readFloat()
    override val mSampleArray = reader.readFloatArray()
}

internal class ConstantClipImpl(reader: ObjectReader): ConstantClip {
    override val data = reader.readFloatArray()
}

internal class ValueConstantImpl(reader: ObjectReader): ValueConstant {
    override val mID = reader.readUInt32()
    override val mTypeID: UInt
    override val mType: UInt
    override val mIndex: UInt

    init {
        val version = reader.unityVersion
        mTypeID = if (version < intArrayOf(5, 5)) {
            reader.readUInt32()
        } else 0u
        mType = reader.readUInt32()
        mIndex = reader.readUInt32()
    }
}

internal class ValueArrayConstantImpl(reader: ObjectReader): ValueArrayConstant {
    override val mValueArray = reader.readArrayOf { ValueConstantImpl(this) }
}

internal class GenericBindingImpl: GenericBinding {
    override val path: UInt
    override val attribute: UInt
    override val script: PPtr<UnityObject>?
    override val typeID: ClassIDType
    override val customType: UByte
    override val isPPtrCurve: UByte
    override val isIntCurve: UByte

    constructor(reader: ObjectReader) {
        path = reader.readUInt32()
        attribute = reader.readUInt32()
        script = PPtrImpl(reader)
        val version = reader.unityVersion
        typeID = ClassIDType.of(
            if (version >= intArrayOf(5, 6)) reader.readInt32() else reader.readUInt16().toInt()
        )
        customType = reader.readUInt8()
        isPPtrCurve = reader.readUInt8()
        isIntCurve = if (version >= intArrayOf(2022, 1)) reader.readUInt8() else 0u
        reader.alignStream()
    }

    constructor(path: UInt, attribute: UInt, typeID: ClassIDType) {
        this.path = path
        this.attribute = attribute
        this.typeID = typeID
        script = null
        customType = 0u
        isPPtrCurve = 0u
        isIntCurve = 0u
    }
}

internal class AnimationClipBindingConstantImpl: AnimationClipBindingConstant {
    override val genericBindings: Array<GenericBindingImpl>
    override val pptrCurveMapping: Array<PPtrImpl<UnityObject>>

    constructor(reader: ObjectReader) {
        genericBindings = reader.readArrayOf { GenericBindingImpl(this) }
        pptrCurveMapping = reader.readArrayOf { PPtrImpl(this) }
    }

    constructor(genericBindingArray: Array<GenericBindingImpl>) {
        genericBindings = genericBindingArray
        pptrCurveMapping = emptyArray()
    }

    override fun findBinding(index: Int): GenericBinding? {
        var curves = 0
        for (b in genericBindings) {
            curves += if (b.typeID == ClassIDType.Transform) {
                when (b.attribute) {
                    1u, 3u, 4u -> 3
                    2u -> 4
                    else -> 1
                }
            } else 1
            if (curves > index) return b
        }
        return null
    }
}

internal class ClipImpl(reader: ObjectReader): Clip {
    override val mStreamedClip = StreamedClipImpl(reader)
    override val mDenseClip = DenseClipImpl(reader)
    override val mConstantClip: ConstantClipImpl?
    override val mBinding: ValueArrayConstantImpl?

    init {
        val version = reader.unityVersion
        mConstantClip = if (version >= intArrayOf(4, 3)) {
            ConstantClipImpl(reader)
        } else null
        mBinding = if (version < intArrayOf(2018, 3)) {
            ValueArrayConstantImpl(reader)
        } else null
    }

    override fun buildGenericBindings(): AnimationClipBindingConstantImpl {
        if (mBinding == null) return AnimationClipBindingConstantImpl(emptyArray())
        val bindings = mutableListOf<GenericBindingImpl>()
        var i = 0
        while (i < mBinding.mValueArray.size) {
            val value = mBinding.mValueArray[i]
            val binding = when (value.mID) {
                //CRC(PositionX)
                //kBindTransformPosition
                4174552735u -> {
                    i += 3
                    GenericBindingImpl(value.mID, 1u, ClassIDType.Transform)
                }
                //CRC(QuaternionX)
                //kBindTransformRotation
                2211994246u -> {
                    i += 4
                    GenericBindingImpl(value.mID, 2u, ClassIDType.Transform)
                }
                //CRC(ScaleX)
                //kBindTransformScale
                1512518241u -> {
                    i += 3
                    GenericBindingImpl(value.mID, 3u, ClassIDType.Transform)
                }
                else -> {
                    i += 1
                    GenericBindingImpl(0u, value.mID, ClassIDType.Animator)
                }
            }
            bindings.add(binding)
        }
        return AnimationClipBindingConstantImpl(bindings.toTypedArray())
    }
}

internal class ValueDeltaImpl(reader: ObjectReader): ValueDelta {
    override val mStart = reader.readFloat()
    override val mStop = reader.readFloat()
}

internal class ClipMuscleConstantImpl(reader: ObjectReader): ClipMuscleConstant {
    override val mDeltaPose: HumanPose
    override val mStartX: XForm
    override val mStopX: XForm?
    override val mLeftFootStartX: XForm
    override val mRightFootStartX: XForm
    override val mMotionStartX: XForm?
    override val mMotionStopX: XForm?
    override val mAverageSpeed: Vector3
    override val mClip: Clip
    override val mStartTime: Float
    override val mStopTime: Float
    override val mOrientationOffsetY: Float
    override val mLevel: Float
    override val mCycleOffset: Float
    override val mAverageAngularSpeed: Float
    override val mIndexArray: IntArray
    override val mValueArrayDelta: Array<ValueDeltaImpl>
    override val mValueArrayReferencePose: FloatArray
    override val mMirror: Boolean
    override val mLoopTime: Boolean
    override val mLoopBlend: Boolean
    override val mLoopBlendOrientation: Boolean
    override val mLoopBlendPositionY: Boolean
    override val mLoopBlendPositionXZ: Boolean
    override val mStartAtOrigin: Boolean
    override val mKeepOriginalOrientation: Boolean
    override val mKeepOriginalPositionY: Boolean
    override val mKeepOriginalPositionXZ: Boolean
    override val mHeightFromFeet: Boolean

    init {
        mDeltaPose = HumanPoseImpl(reader)
        mStartX = XFormImpl(reader)
        val version = reader.unityVersion
        val v55 = intArrayOf(5, 5)
        mStopX = if (version >= v55) XFormImpl(reader) else null
        mLeftFootStartX = XFormImpl(reader)
        mRightFootStartX = XFormImpl(reader)
        if (version[0] < 5) {
            mMotionStartX = XFormImpl(reader)
            mMotionStopX = XFormImpl(reader)
        } else {
            mMotionStartX = null
            mMotionStopX = null
        }
        mAverageSpeed = if (version >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
        mClip = ClipImpl(reader)
        mStartTime = reader.readFloat()
        mStopTime = reader.readFloat()
        mOrientationOffsetY = reader.readFloat()
        mLevel = reader.readFloat()
        mCycleOffset = reader.readFloat()
        mAverageAngularSpeed = reader.readFloat()
        mIndexArray = reader.readInt32Array()
        if (version < intArrayOf(4, 3)) reader.readInt32Array()   //m_AdditionalCurveIndexArray: List<Int>
        mValueArrayDelta = reader.readArrayOf { ValueDeltaImpl(this) }
        mValueArrayReferencePose = if (version >= intArrayOf(5, 3)) reader.readFloatArray() else floatArrayOf()
        mMirror = reader.readBool()
        mLoopTime = if (version >= intArrayOf(4, 3)) reader.readBool() else false
        mLoopBlend = reader.readBool()
        mLoopBlendOrientation = reader.readBool()
        mLoopBlendPositionY = reader.readBool()
        mLoopBlendPositionXZ = reader.readBool()
        mStartAtOrigin = if (version >= v55) reader.readBool() else false
        mKeepOriginalOrientation = reader.readBool()
        mKeepOriginalPositionY = reader.readBool()
        mKeepOriginalPositionXZ = reader.readBool()
        mHeightFromFeet = reader.readBool()
        reader.alignStream()
    }
}

internal class AnimationEventImpl(reader: ObjectReader): AnimationEvent {
    override val time = reader.readFloat()
    override val functionName = reader.readAlignedString()
    override val data = reader.readAlignedString()
    override val objectReferenceParameter = PPtrImpl<UnityObject>(reader)
    override val floatParameter = reader.readFloat()
    override val intParameter = if (reader.unityVersion[0] >= 3) reader.readInt32() else 0
    override val messageOptions = reader.readInt32()
}
