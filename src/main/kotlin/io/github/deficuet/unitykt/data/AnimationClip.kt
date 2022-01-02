package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.file.ClassIDType
import io.github.deficuet.unitykt.math.*
import io.github.deficuet.unitykt.util.*
import kotlin.math.sqrt

class AnimationClip internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mAnimationType: AnimationType
    val mLegacy: Boolean
    val mCompressed: Boolean
    val mUseHighQualityCurve: Boolean
    val mRotationCurves: List<QuaternionCurve>
    val mCompressedRotationCurves: List<CompressedAnimationCurve>
    val mEulerCurves: List<Vector3Curve>
    val mPositionCurves: List<Vector3Curve>
    val mScaleCurves: List<Vector3Curve>
    val mFloatCurves: List<FloatCurve>
    val mPPtrCurves: List<PPtrCurve>
    val mSampleRate: Float
    val mWrapMode: Float
    val mBounds: AABB?
    val mMuscleClipSize: UInt
    val mMuscleClip: ClipMuscleConstant?
    val mClipBindingConstant: AnimationClipBindingConstant?
    val mEvents: List<AnimationEvent>

    init {
        val v43 = intArrayOf(4, 3)
        mLegacy = if (unityVersion[0] >= 5) {
            mAnimationType = AnimationType.default
            reader.readBool()
        } else if (unityVersion[0] >= 4) {
            mAnimationType = AnimationType.of(reader.readInt())
            mAnimationType == AnimationType.kLegacy
        } else {
            mAnimationType = AnimationType.default
            true
        }
        mCompressed = reader.readBool()
        mUseHighQualityCurve = if (unityVersion >= v43) reader.readBool() else false
        reader.alignStream()
//        val numRCurves = reader.readInt()
//        val rotationCurves = mutableListOf<QuaternionCurve>()
//        for (i in 0 until numRCurves) {
//            rotationCurves.add(QuaternionCurve(reader))
//        }
        mRotationCurves = reader.readArrayOf { QuaternionCurve(reader) }
//        val numCRCurves = reader.readInt()
//        val crCurves = mutableListOf<CompressedAnimationCurve>()
//        for (j in 0 until numCRCurves) {
//            crCurves.add(CompressedAnimationCurve(reader))
//        }
        mCompressedRotationCurves = reader.readArrayOf { CompressedAnimationCurve(reader) }
        mEulerCurves = if (unityVersion >= intArrayOf(5, 3)) {
//            val numEulerCurves = reader.readInt()
//            val eCurves = mutableListOf<Vector3Curve>()
//            for (k in 0 until numEulerCurves) {
//                eCurves.add(Vector3Curve(reader))
//            }
//            eCurves
            reader.readArrayOf { Vector3Curve(reader) }
        } else emptyList()
//        val numPCurves = reader.readInt()
//        val pCurves = mutableListOf<Vector3Curve>()
//        for (l in 0 until numPCurves) {
//            pCurves.add(Vector3Curve(reader))
//        }
        mPositionCurves = reader.readArrayOf { Vector3Curve(reader) }
//        val numSCurves = reader.readInt()
//        val sCurves = mutableListOf<Vector3Curve>()
//        for (m in 0 until numSCurves) {
//            sCurves.add(Vector3Curve(reader))
//        }
        mScaleCurves = reader.readArrayOf { Vector3Curve(reader) }
//        val numFCurves = reader.readInt()
//        val fCurves = mutableListOf<FloatCurve>()
//        for (n in 0 until numFCurves) {
//            fCurves.add(FloatCurve(reader))
//        }
        mFloatCurves = reader.readArrayOf { FloatCurve(reader) }
        mPPtrCurves = if (unityVersion >= v43) {
//            val numPtrCurves = reader.readInt()
//            val ptrCurves = mutableListOf<PPtrCurve>()
//            for (o in 0 until numPtrCurves) {
//                ptrCurves.add(PPtrCurve(reader))
//            }
//            ptrCurves
            reader.readArrayOf { PPtrCurve(reader) }
        } else emptyList()
        mSampleRate = reader.readFloat()
        mWrapMode = reader.readFloat()
        mBounds = if (unityVersion >= intArrayOf(3, 4)) AABB(reader) else null
        if (unityVersion[0] >= 4) {
            mMuscleClipSize = reader.readUInt()
            mMuscleClip = ClipMuscleConstant(reader)
        } else {
            mMuscleClipSize = 0u; mMuscleClip = null
        }
        mClipBindingConstant = if (unityVersion >= intArrayOf(4, 3)) AnimationClipBindingConstant(reader) else null
        if (unityVersion >= intArrayOf(2018, 3)) {
            reader += 2     //m_HasGenericRootTransform, m_HasMotionFloatCurves: Boolean
            reader.alignStream()
        }
//        val numEvents = reader.readInt()
//        val events = mutableListOf<AnimationEvent>()
//        for (p in 0 until numEvents) {
//            events.add(AnimationEvent(reader))
//        }
        mEvents = reader.readArrayOf { AnimationEvent(reader) }
        if (unityVersion[0] >= 2017) reader.alignStream()
    }
}

class KeyFrame<T> internal constructor(reader: ObjectReader, readerFunc: () -> T) {
    val time = reader.readFloat()
    val value = readerFunc()
    val inSlope = readerFunc()
    val outSlope = readerFunc()
    val weightedMode: Int
    val inWeight: T?
    val outWeight: T?

    init {
        if (reader.unityVersion[0] >= 2018) {
            weightedMode = reader.readInt()
            inWeight = readerFunc()
            outWeight = readerFunc()
        } else {
            weightedMode = 0; inWeight = null; outWeight = null
        }
    }
}

class AnimationCurve<T> internal constructor(reader: ObjectReader, readerFunc: () -> T) {
    val mCurve = reader.readArrayOf { KeyFrame(reader, readerFunc) }
    val mPreInfinity = reader.readInt()
    val mPostInfinity = reader.readInt()
    val mRotationOrder = if (reader.unityVersion >= intArrayOf(5, 3)) reader.readInt() else 0
}

class QuaternionCurve internal constructor(reader: ObjectReader) {
    val curve = AnimationCurve(reader, reader::readQuaternion)
    val path = reader.readAlignedString()
}

class PackedFloatVector internal constructor(reader: ObjectReader) {
    val mNumItems = reader.readUInt()
    val mRange = reader.readFloat()
    val mStart = reader.readFloat()
    val mData: ByteArray
    val mBitSize: UByte

    init {
        mData = reader.read(reader.readInt())
        reader.alignStream()
        mBitSize = reader.readByte()
        reader.alignStream()
    }

    fun unpackFloats(
        itemCountInChunk: Int,
        chunkStride: Int,
        start: Int = 0,
        chunkCount: Int = -1
    ): FloatArray {
        var bitPos = start * mBitSize.toInt()
        var indexPos = bitPos / 8
        bitPos %= 8
        val scale = 1.0f / mRange
        val numChunks = if (chunkCount == -1) {
            (mNumItems / itemCountInChunk.toUInt()).toInt()
        } else chunkCount
        val end = chunkStride * numChunks / 4
        val data = mutableListOf<Float>()
        for (idx in 0 until end step chunkStride / 4) {
            for (i in 0 until itemCountInChunk) {
                var x = 0u
                var bits = 0
                while (bits < mBitSize.toInt()) {
                    x = x or ((mData[indexPos].toInt() shr bitPos) shl bits).toUInt()
                    val num = minOf(mBitSize.toInt() - bits, 8 - bitPos)
                    bitPos += num; bits += num
                    if (bitPos == 8) {
                        indexPos++; bitPos = 0
                    }
                }
                x = x and ((1 shl mBitSize.toInt()).toUInt() - 1u)
                data.add(x.toFloat() / (scale * ((1 shl mBitSize.toInt()) - 1) + mStart))
            }
        }
        return data.toFloatArray()
    }
}

class PackedIntVector internal constructor(reader: ObjectReader) {
    var mNumItems = reader.readUInt()
        internal set
    val mData: ByteArray
    var mBitSize: UByte
        internal set

    init {
        mData = reader.read(reader.readInt())
        reader.alignStream()
        mBitSize = reader.readByte()
        reader.alignStream()
    }

    fun unpackInts(): IntArray {
        val data = IntArray(mNumItems.toInt())
        var indexPos = 0; var bitPos = 0
        for (i in 0 until mNumItems.toInt()) {
            var bits = 0; var value = 0
            while (bits < mBitSize.toInt()) {
                value = value or ((mData[indexPos].toInt() shr bitPos) shl bits)
                val num = minOf(mBitSize.toInt() - bits, 8 - bitPos)
                bitPos += num; bits += num
                if (bitPos == 8) {
                    indexPos++; bitPos = 0
                }
            }
            data[i] = value and ((1 shl mBitSize.toInt()) - 1)
        }
        return data
    }
}

class PackedQuatVector internal constructor(reader: ObjectReader) {
    val mNumItems = reader.readUInt()
    val mData: ByteArray

    init {
        mData = reader.read(reader.readInt())
        reader.alignStream()
    }

    fun unpackQuats(): Array<Quaternion> {
        val data = arrayOf<Quaternion>()
        var indexPos = 0; var bitPos = 0
        for (i in 0 until mNumItems.toInt()) {
            var flags = 0u; var bits = 0
            while (bits < 3) {
                flags = flags or ((mData[indexPos].toInt() shr bitPos) shl bits).toUInt()
                val num = minOf(3 - bits, 8 - bitPos)
                bitPos += num; bits += num
                if (bitPos == 8) {
                    indexPos++; bitPos = 0
                }
            }
            flags = flags and 7u
            val qFloats = FloatArray(4)
            var sum = 0f
            for (j in 0u..3u) {
                if ((flags and 3u) != j) {
                    val bitSize = if (((flags and 3u) + 1u) % 4u == j) 9 else 10
                    var x = 0u; bits = 0
                    while (bits < bitSize) {
                        x = x or ((mData[indexPos].toInt() shr bitPos) shl bits).toUInt()
                        val num = minOf(bitSize - bits, 8 - bitPos)
                        bitPos += num; bits += num
                        if (bitPos == 8) {
                            indexPos++; bitPos = 0
                        }
                    }
                    x = x and ((1 shl bitSize) - 1).toUInt()
                    val f = x.toFloat() / (0.5f * ((1 shl bitSize) - 1)) - 1
                    sum += f * f
                    qFloats[j.toInt()] = f
                }
            }
            val qLast = (flags and 3u).toInt()
            qFloats[qLast] = sqrt(1 - sum)
            if ((flags and 4u) != 0u) qFloats[qLast] = -qFloats[qLast]
            data[i] = Quaternion(*qFloats)
        }
        return data
    }
}

class CompressedAnimationCurve internal constructor(reader: ObjectReader) {
    val mPath = reader.readAlignedString()
    val mTimes = PackedIntVector(reader)
    val mValues = PackedQuatVector(reader)
    val mSlopes = PackedFloatVector(reader)
    val mPreInfinity = reader.readInt()
    val mPostInfinity = reader.readInt()
}

class Vector3Curve internal constructor(reader: ObjectReader) {
    val curve = AnimationCurve(reader, reader::readVector3)
    val path = reader.readAlignedString()
}

class FloatCurve internal constructor(reader: ObjectReader) {
    val curve = AnimationCurve(reader, reader::readFloat)
    val attribute = reader.readAlignedString()
    val path = reader.readAlignedString()
    val classID: ClassIDType
    val script: PPtr<MonoScript>

    init {
        val id = reader.readInt()
        classID = ClassIDType.of(id)
        script = PPtr(reader)
    }
}

class PPtrKeyFrame internal constructor(reader: ObjectReader) {
    val time = reader.readFloat()
    val value = PPtr<Object>(reader)
}

class PPtrCurve internal constructor(reader: ObjectReader) {
    val curve = reader.readArrayOf { PPtrKeyFrame(reader) }
    val attribute = reader.readAlignedString()
    val path = reader.readAlignedString()
    val classID = reader.readInt()
    val script = PPtr<MonoScript>(reader)

//    init {
//        val curveCount = reader.readInt()
//        val curves = mutableListOf<PPtrKeyFrame>()
//        for (i in 0 until curveCount) {
//            curves.add(PPtrKeyFrame(reader))
//        }
//        curve = curves
//        attribute = reader.readAlignedString()
//        path = reader.readAlignedString()
//        classID = reader.readInt()
//        script = PPtr(reader)
//    }
}

class AABB internal constructor(reader: ObjectReader) {
    val mCenter = reader.readVector3()
    val mExtent = reader.readVector3()
}

class XForm internal constructor(reader: ObjectReader) {
    val t: Vector3
    val q: Quaternion
    val s: Vector3

    init {
        val version = reader.unityVersion
        val v = intArrayOf(5, 4)
        t = if (version >= v) reader.readVector3() else reader.readVector4().vector3
        q = reader.readQuaternion()
        s = if (version >= v) reader.readVector3() else reader.readVector4().vector3
    }
}

class HandPose internal constructor(reader: ObjectReader) {
    val mGrabX = XForm(reader)
    val mDoFArray = reader.readNextFloatArray()
    val mOverride = reader.readFloat()
    val mCloseOpen = reader.readFloat()
    val mInOut = reader.readFloat()
    val mGrab = reader.readFloat()
}

class HumanGoal internal constructor(reader: ObjectReader) {
    val mX = XForm(reader)
    val mWeightT = reader.readFloat()
    val mWeightR = reader.readFloat()
    val mHintT: Vector3
    val mHintWeightT: Float

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

class HumanPose internal constructor(reader: ObjectReader) {
    val mRootX = XForm(reader)
    val mLookAt = if (reader.unityVersion >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
    val mLookAtWeight = reader.readVector4()
    val mGoalArray = reader.readArrayOf { HumanGoal(reader) }
    val mLeftHandPose = HandPose(reader)
    val mRightHandPose = HandPose(reader)
    val mDoFArray = reader.readNextFloatArray()
    val mTDoFArray = if (reader.unityVersion > intArrayOf(5, 2)) {
        reader.readArrayOf {
            if (reader.unityVersion >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
        }
    } else emptyList()

//    init {
//        val version = reader.unityVersion
//        mLookAt = if (version >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
//        mLookAtWeight = reader.readVector4()
////        val goalCount = reader.readInt()
////        val goals = mutableListOf<HumanGoal>()
////        for (i in 0 until goalCount) {
////            goals.add(HumanGoal(reader))
////        }
//        mGoalArray = reader.readArrayOf { HumanGoal(reader) }
//        mLeftHandPose = HandPose(reader)
//        mRightHandPose = HandPose(reader)
//        mDoFArray = reader.readNextFloatArray()
//        mTDoFArray =  if (version > intArrayOf(5, 2)) {
////            val numTDoF = reader.readInt()
////            val tDoFs = mutableListOf<Vector3>()
////            for (j in 0 until numTDoF) {
////                tDoFs.add(
////                    if (version >= intArrayOf(5, 4)) reader.readVector3()
////                    else reader.readVector4().vector3
////                )
////            }
////            tDoFs
//            reader.readArrayOf {
//                if (version >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
//            }
//        } else emptyList()
//    }
}

class StreamedCurveKey internal constructor(reader: EndianBinaryReader) {
    val index = reader.readInt()
    val coeff = reader.readNextFloatArray(4)
    val outSlope = coeff[2]
    val value = coeff[3]
    var inSlope = 0f

    fun nextInSlope(deltaX: Float, rhs: StreamedCurveKey): Float {
        if (coeff.subList(0, 3).all { it == 0f }) return Float.POSITIVE_INFINITY
        val dx = maxOf(deltaX, 0.0001f)
        val dy = rhs.value - value
        val length = 1f / dx / dx
        val d1 = outSlope * dx
        val d2 = dy * 3 - d1 * 2 - coeff[1] / length
        return d2 / dx
    }
}

class StreamedFrame internal constructor(reader: EndianBinaryReader) {
    val time = reader.readFloat()
    val keyList = reader.readArrayOf { StreamedCurveKey(reader) }

//    init {
//        val keyCount = reader.readInt()
//        val keys = mutableListOf<StreamedCurveKey>()
//        for (i in 0 until keyCount) {
//            keys.add(StreamedCurveKey(reader))
//        }
//        keyList = keys
//    }
}

class StreamedClip internal constructor(reader: ObjectReader) {
    val data = reader.readNextUIntArray()
    val curveCount = reader.readUInt()

    fun readData(): List<StreamedFrame> {
        val frameList = mutableListOf<StreamedFrame>()
        val buffer = ByteArray(data.size * 4) {
            data[it / 4].shr(24 - (it % 4) * 8).toByte()
        }
        EndianByteArrayReader(buffer).use {
            while (it.position < it.length) {
                frameList.add(StreamedFrame(it))
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

class DenseClip internal constructor(reader: ObjectReader) {
    val mFrameCount = reader.readInt()
    val mCurveCount = reader.readUInt()
    val mSampleRate = reader.readFloat()
    val mBeginTime = reader.readFloat()
    val mSampleArray = reader.readNextFloatArray()
}

class ConstantClip internal constructor(reader: ObjectReader) {
    val data = reader.readNextFloatArray()
}

class ValueConstant internal constructor(reader: ObjectReader) {
    val mID = reader.readUInt()
    val mTypeID: UInt
    val mType: UInt
    val mIndex: UInt

    init {
        val version = reader.unityVersion
        mTypeID = if (version < intArrayOf(5, 5)) {
            reader.readUInt()
        } else 0u
        mType = reader.readUInt()
        mIndex = reader.readUInt()
    }
}

class ValueArrayConstant internal constructor(reader: ObjectReader) {
    val mValueArray = reader.readArrayOf { ValueConstant(reader) }

//    init {
//        val valCount = reader.readInt()
//        val values = mutableListOf<ValueConstant>()
//        for (i in 0 until valCount) {
//            values.add(ValueConstant(reader))
//        }
//        mValueArray = values
//    }
}

class GenericBinding internal constructor(reader: ObjectReader) {
    val path = reader.readUInt()
    val attribute = reader.readUInt()
    val script = PPtr<Object>(reader)
    val typeID: ClassIDType
    val customType: UByte
    val isPPtrCurve: UByte

    init {
        val version = reader.unityVersion
        typeID = ClassIDType.of(
            if (version >= intArrayOf(5, 6)) reader.readInt() else reader.readUShort().toInt()
        )
        customType = reader.readByte()
        isPPtrCurve = reader.readByte()
        reader.alignStream()
    }
}

class AnimationClipBindingConstant internal constructor(reader: ObjectReader) {
    val genericBindings = reader.readArrayOf { GenericBinding(reader) }
    val pptrCurveMapping = reader.readArrayOf { PPtr<Object>(reader) }

//    internal constructor(reader: ObjectReader) {
//        val bindingCount = reader.readInt()
//        val bindings = mutableListOf<GenericBinding>()
//        for (i in 0 until bindingCount) {
//            bindings.add(GenericBinding(reader))
//        }
//        genericBindings = bindings
//        val mappingCount = reader.readInt()
//        val mappings = mutableListOf<PPtr<Object>>()
//        for (j in 0 until mappingCount) {
//            mappings.add(PPtr(reader))
//        }
//        pptrCurveMapping = mappings
//    }
//
//    internal constructor(bindings: List<GenericBinding>) {
//        genericBindings = bindings; pptrCurveMapping = emptyList()
//    }

    fun findBinding(index: Int): GenericBinding? {
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

class Clip internal constructor(reader: ObjectReader) {
    val mStreamedClip = StreamedClip(reader)
    val mDenseClip = DenseClip(reader)
    val mConstantClip: ConstantClip?
    val mBinding: ValueArrayConstant?

    init {
        val version = reader.unityVersion
        mConstantClip = if (version >= intArrayOf(4, 3)) {
            ConstantClip(reader)
        } else null
        mBinding = if (version >= intArrayOf(2018, 3)) {
            ValueArrayConstant(reader)
        } else null
    }
}

class ValueDelta internal constructor(reader: ObjectReader) {
    val mStart = reader.readFloat()
    val mStop = reader.readFloat()
}

class ClipMuscleConstant internal constructor(reader: ObjectReader) {
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
    val mIndexArray: List<Int>
    val mValueArrayDelta: List<ValueDelta>
    val mValueArrayReferencePose: List<Float>
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

    init {
        mDeltaPose = HumanPose(reader)
        mStartX = XForm(reader)
        val version = reader.unityVersion
        val v55 = intArrayOf(5, 5)
        mStopX = if (version > v55) XForm(reader) else null
        mLeftFootStartX = XForm(reader)
        mRightFootStartX = XForm(reader)
        if (version[0] < 5) {
            mMotionStartX = XForm(reader)
            mMotionStopX = XForm(reader)
        } else {
            mMotionStartX = null
            mMotionStopX = null
        }
        mAverageSpeed = if (version >= intArrayOf(5, 4)) reader.readVector3() else reader.readVector4().vector3
        mClip = Clip(reader)
        mStartTime = reader.readFloat()
        mStopTime = reader.readFloat()
        mOrientationOffsetY = reader.readFloat()
        mLevel = reader.readFloat()
        mCycleOffset = reader.readFloat()
        mAverageAngularSpeed = reader.readFloat()
        mIndexArray = reader.readNextIntArray()
        if (version < intArrayOf(4, 3)) reader.readNextIntArray()   //m_AdditionalCurveIndexArray: List<Int>
        mValueArrayDelta = reader.readArrayOf { ValueDelta(reader) }
        mValueArrayReferencePose = if (version >= intArrayOf(5, 3)) reader.readNextFloatArray() else emptyList()
        mMirror = reader.readBool()
        mLoopTime = if (version >= intArrayOf(4, 3)) reader.readBool() else false
        mLoopBlend = reader.readBool()
        mLoopBlendOrientation = reader.readBool()
        mLoopBlendPositionY = reader.readBool()
        mLoopBlendPositionXZ = reader.readBool()
        mStartAtOrigin = if (version > v55) reader.readBool() else false
        mKeepOriginalOrientation = reader.readBool()
        mKeepOriginalPositionY = reader.readBool()
        mKeepOriginalPositionXZ = reader.readBool()
        mHeightFromFeet = reader.readBool()
        reader.alignStream()
    }
}

class AnimationEvent internal constructor(reader: ObjectReader) {
    val time = reader.readFloat()
    val functionName = reader.readAlignedString()
    val data = reader.readAlignedString()
    val objectReferenceParameter = PPtr<Object>(reader)
    val floatParameter = reader.readFloat()
    val intParameter: Int
    val messageOptions: Int

    init {
        intParameter = if (reader.unityVersion[0] >= 3) reader.readInt() else 0
        messageOptions = reader.readInt()
    }
}

@Suppress("EnumEntryName")
enum class AnimationType(val id: Int) {
    default(0), kLegacy(1), kGeneric(2), kHumanoid(3);

    companion object {
        fun of(value: Int): AnimationType {
            return values().firstOrNull { it.id == value } ?: default
        }
    }
}