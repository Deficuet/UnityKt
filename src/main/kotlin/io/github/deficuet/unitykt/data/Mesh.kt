package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.data.MeshHelper.Companion.toVertexFormat
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.util.*
import java.nio.ByteBuffer
import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.sqrt

class Mesh internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mSubMeshes: Array<SubMash>
    val mShapes: BlendShapeData?
    val mIndices: Array<UInt>
    var mBindPose: Array<Matrix4x4> = emptyArray()
        private set
    var mBoneNameHashes: Array<UInt> = emptyArray()
        private set
    var mVertexCount: Int = 0
        private set
    var mVertices: FloatArray = floatArrayOf()
        private set
    var mSkin: Array<BoneWeights4> = emptyArray()
        private set
    var mNormals: FloatArray = floatArrayOf()
        private set
    var mColors: FloatArray = floatArrayOf()
        private set
    var mUV0: FloatArray = floatArrayOf()
        private set
    var mUV1: FloatArray = floatArrayOf()
        private set
    var mUV2: FloatArray = floatArrayOf()
        private set
    var mUV3: FloatArray = floatArrayOf()
        private set
    var mUV4: FloatArray = floatArrayOf()
        private set
    var mUV5: FloatArray = floatArrayOf()
        private set
    var mUV6: FloatArray = floatArrayOf()
        private set
    var mUV7: FloatArray = floatArrayOf()
        private set
    var mTangents: FloatArray = floatArrayOf()
        private set
    private var mUse16BitIndices: Boolean
    private var mIndexBuffer: Array<UInt> = emptyArray()
    private lateinit var mVertexData: VertexData
    private val mCompressedMesh: CompressedMesh?
    private val mStreamData: StreamingInfo?

    init {
        val indices = mutableListOf<UInt>()
        mUse16BitIndices = if (unityVersion < intArrayOf(3, 5)) reader.readInt() > 0 else true
        if (unityVersion <= intArrayOf(2, 5)) {
            val indexBufferSize = reader.readInt()
            if (mUse16BitIndices) {
                mIndexBuffer = reader.readArrayOf(indexBufferSize / 2) { reader.readUShort().toUInt() }
                reader.alignStream()
            } else {
                mIndexBuffer = reader.readNextUIntArray(indexBufferSize / 4)
            }
        }
        mSubMeshes = reader.readArrayOf { SubMash(reader) }
        mShapes = if (unityVersion >= intArrayOf(4, 1)) BlendShapeData(reader) else null
        if (unityVersion >= intArrayOf(4, 3)) {
            mBindPose = reader.readNextMatrixArray()
            mBoneNameHashes = reader.readNextUIntArray()
            reader += 4     //m_RootBoneNameHash: UInt
        }
        if (unityVersion >= intArrayOf(2, 6)) {
            if (unityVersion[0] >= 2019) {
                reader.readArrayOf { MinMaxAABB(reader) }   //m_BonesAABB
                reader.readNextUIntArray()      //m_VariableBoneCountWeights
            }
            val meshCompression = reader.readByte()
            if (unityVersion[0] >= 4) {
                if (unityVersion[0] < 5) {
                    reader += 1     //m_StreamCompression: UByte
                }
                reader += 3     //m_IsReadable, m_KeepVertices, m_KeepIndices: Boolean
            }
            reader.alignStream()
            if (
                unityVersion >= intArrayOf(2017, 4) ||
                ((unityVersion[0] == 2017 && unityVersion[1] == 3 && unityVersion[2] == 1) && buildType.isPatch) ||
                ((unityVersion[0] == 2017 && unityVersion[1] == 3) && meshCompression == 0u.toUByte())
            ) {
                mUse16BitIndices = reader.readInt() == 0
            }
            val indexBufferSize = reader.readInt()
            if (mUse16BitIndices) {
                mIndexBuffer = reader.readArrayOf(indexBufferSize / 2) { reader.readUShort().toUInt() }
                reader.alignStream()
            } else {
                mIndexBuffer = reader.readNextUIntArray(indexBufferSize / 4)
            }
        }
        if (unityVersion < intArrayOf(3, 5)) {
            mVertexCount = reader.readInt()
            mVertices = reader.readNextFloatArray(mVertexCount * 3)
            mSkin = reader.readArrayOf { BoneWeights4(reader) }
            mBindPose = reader.readNextMatrixArray()
            mUV0 = reader.readNextFloatArray(reader.readInt() * 2)
            mUV1 = reader.readNextFloatArray(reader.readInt() * 2)
            if (unityVersion[0] == 2 && unityVersion[1] <= 5) {
                val tangentSpaceSize = reader.readInt()
                val normals = FloatArray(tangentSpaceSize * 3)
                val tangents = FloatArray(tangentSpaceSize * 4)
                for (v in 0 until tangentSpaceSize) {
                    normals[v * 3] = reader.readFloat()
                    normals[v * 3 + 1] = reader.readFloat()
                    normals[v * 3 + 2] = reader.readFloat()
                    tangents[v * 3] = reader.readFloat()
                    tangents[v * 3 + 1] = reader.readFloat()
                    tangents[v * 3 + 2] = reader.readFloat()
                    tangents[v * 3 + 3] = reader.readFloat()
                }
                mNormals = normals
                mTangents = tangents
            } else {
                mNormals = reader.readNextFloatArray(reader.readInt() * 3)
                mTangents = reader.readNextFloatArray(reader.readInt() * 4)
            }
        } else {
            if (unityVersion < intArrayOf(2018, 2)) {
                mSkin = reader.readArrayOf { BoneWeights4(reader) }
            }
            if (unityVersion[0] == 3 || (unityVersion[0] == 4 && unityVersion[1] <= 2)) {
                mBindPose = reader.readNextMatrixArray()
            }
            mVertexData = VertexData(reader)
        }
        mCompressedMesh = if (unityVersion >= intArrayOf(2, 6)) {
            CompressedMesh(reader)
        } else null
        reader += 24
        if (unityVersion <= intArrayOf(3, 4)) {
            mColors = reader.readArrayOf(reader.readInt() * 4) {
                (reader.readByte() / 0xFFu).toFloat()
            }.toFloatArray()
            reader += reader.readInt() * 4 + 4    //m_CollisionVertexCount
        }
        reader += 4     //m_MeshUsageFlags: Int
        if (unityVersion[0] >= 5) {
            reader.readNextByteArray()      //m_BakedConvexCollisionMesh
            reader.alignStream()
            reader.readNextByteArray()      //m_BakedTriangleCollisionMesh
            reader.alignStream()
        }
        if (unityVersion >= intArrayOf(2018, 2)) {
            reader += 8     //m_MeshMetrics: float[2]
        }
        mStreamData = if (unityVersion >= intArrayOf(2018, 3)) {
            reader.alignStream()
            StreamingInfo(reader)
        } else null
        //region processData
        if (mStreamData?.path?.isNotEmpty() == true) {
            if (mVertexData.mVertexCount > 0u) {
                println(mStreamData.path)
                val resourceReader = ResourceReader(
                    mStreamData.path, asserFile, mStreamData.offset, mStreamData.size.toLong()
                )
                mVertexData.mDataSize = resourceReader.bytes
            }
        }
        if (unityVersion >= intArrayOf(3, 5)) {
            //region readVertexData
            mVertexCount = mVertexData.mVertexCount.toInt()
            for (chn in mVertexData.mChannels.indices) {
                val channel = mVertexData.mChannels[chn]
                if (channel.dimension > 0u) {
                    val stream = mVertexData.mStreams[channel.stream.toInt()]
                    val mask = BitSet.valueOf(longArrayOf(stream.channelMask.toLong()))
                    if (mask[chn]) {
                        if (unityVersion[0] < 2018 && chn == 2 && channel.format == 2u.toUByte()) {
                            channel.dimension = 4u
                        }
                        val vertexFormat = channel.format.toVertexFormat(unityVersion)
                        val componentByteSize = vertexFormat.size.toInt()
                        val componentBytes = ByteArray(
                            mVertexCount * channel.dimension.toInt() * componentByteSize
                        )
                        for (v in 0 until mVertexCount) {
                            val vertexOffset = (stream.offset + channel.offset + stream.stride * v.toUInt()).toInt()
                            for (d in 0 until channel.dimension.toInt()) {
                                val componentOffset = vertexOffset + componentByteSize * d
                                System.arraycopy(
                                    mVertexData.mDataSize,
                                    componentOffset,
                                    componentBytes,
                                    componentByteSize * (v * channel.dimension.toInt() + d),
                                    componentByteSize
                                )
                            }
                        }
                        if (reader.endian == EndianType.LittleEndian && componentByteSize > 1) {
                            componentBytes.reverse()
                        }
                        val array = if (vertexFormat.isIntFormat()) {
                            componentBytes.toIntArray(vertexFormat)
                        } else {
                            componentBytes.toFloatArray(vertexFormat)
                        }
                        if (unityVersion[0] >= 2018) {
                            when (chn) {
                                0 -> mVertices = array as FloatArray
                                1 -> mNormals = array as FloatArray
                                2 -> mTangents = array as FloatArray
                                3 -> mColors = array as FloatArray
                                4 -> mUV0 = array as FloatArray
                                5 -> mUV1 = array as FloatArray
                                6 -> mUV2 = array as FloatArray
                                7 -> mUV3 = array as FloatArray
                                8 -> mUV4 = array as FloatArray
                                9 -> mUV5 = array as FloatArray
                                10 -> mUV6 = array as FloatArray
                                11 -> mUV7 = array as FloatArray
                                12 -> {
                                    if (mSkin.isEmpty()) {
                                        mSkin = reader.readArrayOf(mVertexCount) { BoneWeights4() }
                                    }
                                    for (i in 0 until mVertexCount) {
                                        for (j in 0 until channel.dimension.toInt()) {
                                            mSkin[i].weight[j] = (array as FloatArray)[i * channel.dimension.toInt() + j]
                                        }
                                    }
                                }
                                13 -> {
                                    if (mSkin.isEmpty()) {
                                        mSkin = reader.readArrayOf(mVertexCount) { BoneWeights4() }
                                    }
                                    for (i in 0 until mVertexCount) {
                                        for (j in 0 until channel.dimension.toInt()) {
                                            mSkin[i].boneIndex[j] = (array as IntArray)[i * channel.dimension.toInt() + j]
                                        }
                                    }
                                }
                            }
                        } else {
                            when (chn) {
                                0 -> mVertices = array as FloatArray
                                1 -> mNormals = array as FloatArray
                                2 -> mColors = array as FloatArray
                                3 -> mUV0 = array as FloatArray
                                4 -> mUV1 = array as FloatArray
                                5 -> {
                                    if (unityVersion[0] >= 5) {
                                        mUV2 = array as FloatArray
                                    } else {
                                        mTangents = array as FloatArray
                                    }
                                }
                                6 -> mUV3 = array as FloatArray
                                7 -> mTangents = array as FloatArray
                            }
                        }
                    }
                }
            }
            //endregion
        }
        if (unityVersion >= intArrayOf(2, 6)) {
            //region DecompressCompressedMesh
            if (mCompressedMesh!!.mVertices.mNumItems > 0u) {
                mVertexCount = (mCompressedMesh.mVertices.mNumItems / 3u).toInt()
                mVertices = mCompressedMesh.mVertices.unpackFloats(3, 12)
            }
            if (mCompressedMesh.mUV.mNumItems > 0u) {
                val uvInfo = mCompressedMesh.mUVInfo
                if (uvInfo != 0u) {
                    var uvSrcOffset = 0
                    for (uv in 0 until kMaxTexCoordShaderChannels) {
                        var texCoordBits = uvInfo.shr(uv * kInfoBitsPerUV)
                        texCoordBits = texCoordBits.and(1u.shl(kInfoBitsPerUV) - 1u)
                        if (texCoordBits.and(kUVChannelExists) != 0u) {
                            val uvDim = 1 + texCoordBits.and(kUVDimensionMask).toInt()
                            val mUV = mCompressedMesh.mUV.unpackFloats(
                                uvDim, uvDim * 4, uvSrcOffset, mVertexCount
                            )
                            //region setUV
                            when (uv) {
                                0 -> mUV0 = mUV
                                1 -> mUV1 = mUV
                                2 -> mUV2 = mUV
                                3 -> mUV3 = mUV
                                4 -> mUV4 = mUV
                                5 -> mUV5 = mUV
                                6 -> mUV6 = mUV
                                7 -> mUV7 = mUV
                                else -> throw IndexOutOfBoundsException()
                            }
                            //endregion
                            uvSrcOffset += uvDim * mVertexCount
                        }
                    }
                } else {
                    mUV0 = mCompressedMesh.mUV.unpackFloats(
                        2, 8, 0, mVertexCount
                    )
                    if (mCompressedMesh.mUV.mNumItems >= (mVertexCount * 4).toUInt()) {
                        mUV1 = mCompressedMesh.mUV.unpackFloats(
                            2, 8, mVertexCount * 2, mVertexCount
                        )
                    }
                }
            }
            if (unityVersion[0] < 5) {
                val unpackedBindPose = mCompressedMesh.mBindPoses!!.unpackFloats(16, 64)
                val buffer = FloatArray(16)
                mBindPose = reader.readArrayIndexedOf(
                    (mCompressedMesh.mBindPoses.mNumItems / 16u).toInt()
                ) {
                    System.arraycopy(
                        unpackedBindPose, it * 16,
                        buffer, 0, 16
                    )
                    Matrix4x4(*buffer)
                }
            }
            if (mCompressedMesh.mNormals.mNumItems > 0u) {
                val normalData = mCompressedMesh.mNormals.unpackFloats(2, 8)
                val signs = mCompressedMesh.mNormalSigns.unpackInts()
                val normals = FloatArray((mCompressedMesh.mNormals.mNumItems / 2u * 3u).toInt())
                for (i in 0 until (mCompressedMesh.mNormals.mNumItems / 2u).toInt()) {
                    var x = normalData[i * 2]
                    var y = normalData[i * 2 + 1]
                    val zsqr = 1 - x * x - y * y
                    var z: Float
                    if (zsqr >= 0f) {
                        z = sqrt(zsqr)
                    } else {
                        z = 0f
                        with(Vector3(x, y, z)) {
                            normalize()
                            x = this.x.toFloat()
                            y = this.y.toFloat()
                            z = this.z.toFloat()
                        }
                    }
                    if (signs[i] == 0) z = -z
                    normals[i * 3] = x
                    normals[i * 3 + 1] = y
                    normals[i * 3 + 2] = z
                }
                mNormals = normals
            }
            if (mCompressedMesh.mTangents.mNumItems > 0u) {
                val tangentData = mCompressedMesh.mTangents.unpackFloats(2, 8)
                val signs = mCompressedMesh.mTangentSigns.unpackInts()
                val tangents = FloatArray((mCompressedMesh.mTangents.mNumItems / 2u * 4u).toInt())
                for (i in 0 until mCompressedMesh.mTangents.mNumItems.toInt() / 2) {
                    var x = tangentData[i * 2]
                    var y = tangentData[i * 2 + 1]
                    val zsqr = 1 - x * x - y * y
                    var z: Float
                    if (zsqr >= 0f) {
                        z = sqrt(zsqr)
                    } else {
                        z = 0f
                        with(Vector3(x, y, z)) {
                            normalize()
                            x = this.x.toFloat()
                            y = this.y.toFloat()
                            z = this.z.toFloat()
                        }
                    }
                    if (signs[i * 2] == 0) z = -z
                    val w = if (signs[i * 2 + 1] > 0) 1f else -1f
                    tangents[i * 4] = x
                    tangents[i * 4 + 1] = y
                    tangents[i * 4 + 2] = z
                    tangents[i * 4 + 3] = w
                }
                mTangents = tangents
            }
            if (unityVersion[0] >= 5) {
                if (mCompressedMesh.mFloatColors!!.mNumItems > 0u) {
                    mColors = mCompressedMesh.mFloatColors.unpackFloats(1, 4)
                }
            }
            if (mCompressedMesh.mWeights.mNumItems > 0u) {
                val weights = mCompressedMesh.mWeights.unpackInts()
                val boneIndices = mCompressedMesh.mBoneIndices.unpackInts()
                mSkin = reader.readArrayOf(mVertexCount) { BoneWeights4() }
                var bonePos = 0; var boneIndexPos = 0; var j = 0; var sum = 0
                for (i in 0 until mCompressedMesh.mWeights.mNumItems.toInt()) {
                    with(mSkin[bonePos]) {
                        weight[j] = weights[i] / 31f
                        boneIndex[j] = boneIndices[boneIndexPos++]
                    }
                    j++; sum += weights[i]
                    if (sum >= 31) {
                        while (j < 4) {
                            with(mSkin[bonePos]) {
                                weight[j] = 0f
                                boneIndex[j] = 0
                            }
                            j++
                        }
                        bonePos++
                        j = 0; sum = 0
                    } else if (j == 3) {
                        with(mSkin[bonePos]) {
                            weight[j] = (31 - sum) / 31f
                            boneIndex[j] = boneIndices[boneIndexPos++]
                        }
                        bonePos++
                        j = 0; sum = 0
                    }
                }
            }
            if (mCompressedMesh.mTriangles.mNumItems > 0u) {
                mIndexBuffer = with(mCompressedMesh.mTriangles.unpackInts()) { Array(size) { this[it].toUInt() } }
            }
            if (with(mCompressedMesh.mColors) { this != null && mNumItems > 0u }) {
                mCompressedMesh.mColors!!.mNumItems *= 4u
                mCompressedMesh.mColors!!.mBitSize = (mCompressedMesh.mColors.mBitSize / 4u).toUByte()
                val num = mCompressedMesh.mColors.mNumItems.toInt()
                val tempColors = mCompressedMesh.mColors.unpackInts()
                val colors = FloatArray(num)
                for (v in 0 until num) {
                    colors[v] = tempColors[v] / 255f
                }
            }
            //endregion
        }
        //region getTriangle
        for (subMesh in mSubMeshes) {
            var firstIdx = (subMesh.firstByte / 2u).toInt()
            if (!mUse16BitIndices) {
                firstIdx /= 2
            }
            val indexCount = subMesh.indexCount.toInt()
            if (subMesh.topology == GfxPrimitiveType.kPrimitiveTriangles) {
                for (i in 0 until indexCount step 3) {
                    with(indices) {
                        add(mIndexBuffer[firstIdx + i])
                        add(mIndexBuffer[firstIdx + i + 1])
                        add(mIndexBuffer[firstIdx + i + 2])
                    }
                }
            } else if (unityVersion[0] < 4 && subMesh.topology == GfxPrimitiveType.kPrimitiveTriangleStrip) {
                var triIndex = 0u
                for (i in 0 until indexCount - 2) {
                    val a = mIndexBuffer[firstIdx + i]
                    val b = mIndexBuffer[firstIdx + i + 1]
                    val c = mIndexBuffer[firstIdx + i + 2]
                    if (a == b || a == c || b == c) continue
                    if (i.and(1) == 1) {
                        indices.add(b)
                        indices.add(a)
                    } else {
                        indices.add(a)
                        indices.add(b)
                    }
                    indices.add(c)
                    triIndex += 3u
                }
                subMesh.indexCount = triIndex
            } else if (subMesh.topology == GfxPrimitiveType.kPrimitiveQuads) {
                for (q in 0 until indexCount step 4) {
                    for (x in intArrayOf(0, 1, 2, 0, 2, 3)) {
                        indices.add(mIndexBuffer[firstIdx + q + x])
                    }
                }
            } else {
                throw UnsupportedFormatException("Failed getting triangles. Submesh topology is lines or points.")
            }
        }
        //endregion
        //endregion
        mIndices = indices.toTypedArray()
    }

    private fun ByteArray.toIntArray(vertexFormat: VertexFormat): IntArray {
        val len = size / vertexFormat.size.toInt()
        val result = IntArray(len)
        for (i in 0 until len) {
            when (vertexFormat) {
                VertexFormat.kVertexFormatUInt8,
                VertexFormat.kVertexFormatSInt8 -> result[i] = this[i].toIntBits()
                VertexFormat.kVertexFormatUInt16,
                VertexFormat.kVertexFormatSInt16 -> {
                    result[i] = ByteBuffer.wrap(sliceArray(i * 2.. i * 2 + 1)).short.toInt()
                }
                VertexFormat.kVertexFormatUInt32,
                VertexFormat.kVertexFormatSInt32 -> {
                    result[i] = ByteBuffer.wrap(sliceArray(i * 4..i * 4 + 3)).int
                }
                else -> {  }
            }
        }
        return result
    }

    private fun ByteArray.toFloatArray(vertexFormat: VertexFormat): FloatArray {
        val len = size / vertexFormat.size.toInt()
        val result = FloatArray(len)
        for (i in 0 until len) {
            when (vertexFormat) {
                VertexFormat.kVertexFormatFloat -> {
                    result[i] = ByteBuffer.wrap(sliceArray(i * 4..i * 4 + 3)).float
                }
                VertexFormat.kVertexFormatFloat16 -> {
                    result[i] = sliceArray(i * 2..i * 2 + 1).toHalf()
                }
                VertexFormat.kVertexFormatUNorm8 -> {
                    result[i] = maxOf(get(i) / 127f, -1f)
                }
                VertexFormat.kVertexFormatSNorm8 -> {
                    result[i] = ByteBuffer.wrap(sliceArray(i * 2..i * 2 + 1)).short
                        .toUShort().toInt() / 65536f
                }
                VertexFormat.kVertexFormatSNorm16 -> {
                    result[i] = maxOf(
                        ByteBuffer.wrap(sliceArray(i * 2..i * 2 + 1)).short / 32767f,
                        -1f
                    )
                }
                else -> {  }
            }
        }
        return result
    }

    companion object {
        private const val kInfoBitsPerUV = 4
        private const val kUVDimensionMask = 3u
        private const val kUVChannelExists = 4u
        private const val kMaxTexCoordShaderChannels = 8
    }
}

internal class MeshHelper private constructor() {
    companion object {
        private val vertexFormatMap = mapOf(
            VertexChannelFormat.kChannelFormatFloat to VertexFormat.kVertexFormatFloat,
            VertexChannelFormat.kChannelFormatFloat16 to VertexFormat.kVertexFormatFloat16,
            VertexChannelFormat.kChannelFormatColor to VertexFormat.kVertexFormatUNorm8,
            VertexChannelFormat.kChannelFormatByte to VertexFormat.kVertexFormatUInt8,
            VertexChannelFormat.kChannelFormatUInt32 to VertexFormat.kVertexFormatUInt32
        )
        private val vertexFormat2017Map = mapOf(
            *(VertexFormat.values().map { vf ->
                VertexFormat2017.valueOf(vf.name) to vf
            } + listOf(VertexFormat2017.kVertexFormatColor to VertexFormat.kVertexFormatUNorm8))
                .toTypedArray()
        )
        internal fun UByte.toVertexFormat(version: IntArray): VertexFormat {
            return if (version[0] < 2017) {
                val vcf = VertexChannelFormat.of(toInt())
                vertexFormatMap[vcf] ?: throw NoSuchElementException(vcf.name)
            } else if (version[0] < 2019) {
                val vf2017 = VertexFormat2017.of(toInt())
                vertexFormat2017Map[vf2017] ?: throw NoSuchElementException(vf2017.name)
            } else {
                VertexFormat.of(toInt())
            }
        }
    }
}

@Suppress("EnumEntryName")
internal enum class VertexChannelFormat {
    kChannelFormatFloat,
    kChannelFormatFloat16,
    kChannelFormatColor,
    kChannelFormatByte,
    kChannelFormatUInt32;

    companion object {
        fun of(value: Int): VertexChannelFormat {
            return values()[value]
        }
    }
}

@Suppress("EnumEntryName")
internal enum class VertexFormat2017 {
    kVertexFormatFloat,
    kVertexFormatFloat16,
    kVertexFormatColor,
    kVertexFormatUNorm8,
    kVertexFormatSNorm8,
    kVertexFormatUNorm16,
    kVertexFormatSNorm16,
    kVertexFormatUInt8,
    kVertexFormatSInt8,
    kVertexFormatUInt16,
    kVertexFormatSInt16,
    kVertexFormatUInt32,
    kVertexFormatSInt32;

    companion object {
        fun of(value: Int): VertexFormat2017 {
            return values()[value]
        }
    }
}

@Suppress("EnumEntryName")
internal enum class VertexFormat(val size: UInt) {
    kVertexFormatFloat(4u),
    kVertexFormatFloat16(2u),
    kVertexFormatUNorm8(1u),
    kVertexFormatSNorm8(1u),
    kVertexFormatUNorm16(2u),
    kVertexFormatSNorm16(2u),
    kVertexFormatUInt8(1u),
    kVertexFormatSInt8(1u),
    kVertexFormatUInt16(2u),
    kVertexFormatSInt16(2u),
    kVertexFormatUInt32(4u),
    kVertexFormatSInt32(4u);

    fun isIntFormat() = this >= kVertexFormatUInt8

    companion object {
        fun of(value: Int): VertexFormat {
            return values()[value]
        }
    }
}

class MinMaxAABB internal constructor(reader: EndianBinaryReader) {
    val mMin = reader.readVector3()
    val mMax = reader.readVector3()
}

class CompressedMesh internal constructor(reader: ObjectReader) {
    val mVertices = PackedFloatVector(reader)
    val mUV = PackedFloatVector(reader)
    val mBindPoses = if (reader.unityVersion[0] < 5) PackedFloatVector(reader) else null
    val mNormals = PackedFloatVector(reader)
    val mTangents = PackedFloatVector(reader)
    val mWeights = PackedIntVector(reader)
    val mNormalSigns = PackedIntVector(reader)
    val mTangentSigns = PackedIntVector(reader)
    val mFloatColors = if (reader.unityVersion[0] >= 5) PackedFloatVector(reader) else null
    val mBoneIndices = PackedIntVector(reader)
    val mTriangles = PackedIntVector(reader)
    val mColors: PackedIntVector?
    val mUVInfo: UInt

    init {
        if (reader.unityVersion >= intArrayOf(3, 5)) {
            if (reader.unityVersion[0] < 5) {
                mColors = PackedIntVector(reader)
                mUVInfo = 0u
            } else {
                mUVInfo = reader.readUInt()
                mColors = null
            }
        } else {
            mUVInfo = 0u
            mColors = null
        }
    }
}

class StreamInfo {
    val channelMask: UInt
    val offset: UInt
    val stride: UInt
    val align: UInt
    val dividerOp: UByte
    val frequency: UShort

    internal constructor(reader: ObjectReader) {
        channelMask = reader.readUInt()
        offset = reader.readUInt()
        if (reader.unityVersion[0] < 4) {
            stride = reader.readUInt()
            align  = reader.readUInt()
            dividerOp = 0u
            frequency = 0u
        } else {
            stride = reader.readByte().toUInt()
            dividerOp = reader.readByte()
            frequency = reader.readUShort()
            align = 0u
        }
    }

    internal constructor(c: UInt, o: UInt, s: UInt, d: UByte, f: UShort) {
        channelMask = c
        offset = o
        stride = s
        align = 0u
        dividerOp = d
        frequency = f
    }
}

class ChannelInfo {
    val stream: UByte
    val offset: UByte
    val format: UByte
    var dimension: UByte
        internal set

    internal constructor(reader: ObjectReader) {
        stream = reader.readByte()
        offset = reader.readByte()
        format = reader.readByte()
        dimension = reader.readByte() and 0xfu
    }

    internal constructor(data: ChannelInfoInternal) {
        stream = data.stream
        offset = data.offset
        format = data.format
        dimension = data.dimension
    }
}

internal data class ChannelInfoInternal(
    var stream: UByte = 0u,
    var offset: UByte = 0u,
    var format: UByte = 0u,
    var dimension: UByte = 0u
)

class VertexData internal constructor(reader: ObjectReader) {
    val mCurrentChannels = if (reader.unityVersion[0] < 2018) reader.readUInt() else 0u
    val mVertexCount = reader.readUInt()
    val mChannels: Array<ChannelInfo>
    val mStreams: Array<StreamInfo>
    var mDataSize: ByteArray
        internal set

    init {
        val version = reader.unityVersion
        var channels = emptyArray<ChannelInfo>()
        if (version[0] >= 4) {
            channels = reader.readArrayOf { ChannelInfo(reader) }
        }
        if (version[0] < 5) {
            val streamSize = if (version[0] < 4) 4 else reader.readInt()
            mStreams = reader.readArrayOf(streamSize) { StreamInfo(reader) }
            if (version[0] < 4) {
                //region getChannels
                val internalChannels = mutableListOf<ChannelInfoInternal>()
                for (i in 1..6) internalChannels.add(ChannelInfoInternal())
                for (s in mStreams.indices) {
                    val stream = mStreams[s]
                    val channelMask = BitSet.valueOf(longArrayOf(stream.channelMask.toLong()))
                    var offset: UByte = 0u
                    for (j in 0..5) {
                        if (channelMask[j]) {
                            val channel = internalChannels[j]
                            channel.stream = s.toUByte()
                            channel.offset = offset
                            when (j) {
                                0, 1 -> {
                                    channel.format = 0u
                                    channel.dimension = 3u
                                }
                                2 -> {
                                    channel.format = 2u
                                    channel.dimension = 4u
                                }
                                3, 4 -> {
                                    channel.format = 0u
                                    channel.dimension = 2u
                                }
                                5 -> {
                                    channel.format = 0u
                                    channel.dimension = 4u
                                }
                            }
                            val inc = channel.dimension * channel.format.toVertexFormat(version).size
                            offset = (offset + inc).toUByte()
                        }
                    }
                }
                channels = with(internalChannels) { Array(size) { ChannelInfo(this[it]) } }
                //endregion
            }
        } else {
            //region getStream
            val streams = mutableListOf<StreamInfo>()
            val streamCount = channels.maxOf { it.stream }.toInt() + 1
            var offset = 0u
            for (k in 0 until streamCount) {
                var chnMask = 0u
                var stride = 0u
                for (chn in channels.indices) {
                    val channel = channels[chn]
                    if (channel.stream.toInt() == k && channel.dimension > 0u) {
                        chnMask = chnMask.or(1u.shl(chn))
                        stride += channel.dimension * channel.format.toVertexFormat(version).size
                    }
                }
                streams.add(StreamInfo(
                    chnMask, offset, stride, 0u, 0u
                ))
                offset += mVertexCount * stride
                offset = (offset + 15u).and(15u.inv())
            }
            mStreams = streams.toTypedArray()
            //endregion
        }
        mDataSize = reader.readNextByteArray()
        mChannels = channels
    }
}

class BoneWeights4 {
    val weight: FloatArray
    val boneIndex: IntArray

    internal constructor(reader: ObjectReader) {
        weight = reader.readNextFloatArray(4)
        boneIndex = reader.readNextIntArray(4)
    }

    internal constructor() {
        weight = FloatArray(4)
        boneIndex = IntArray(4)
    }
}

class BlendShapeVertex internal constructor(reader: ObjectReader) {
    val vertex = reader.readVector3()
    val normal = reader.readVector3()
    val tangent = reader.readVector3()
    val index = reader.readUInt()
}

class MeshBlendShape internal constructor(reader: ObjectReader) {
    val firstVertex: UInt
    val vertexCount: UInt
    val hasNormals: Boolean
    val hasTangents: Boolean

    init {
        val version = reader.unityVersion
        val v43 = intArrayOf(4, 3)
        if (version < v43) {
            reader.readAlignedString()      //name
        }
        firstVertex = reader.readUInt()
        vertexCount = reader.readUInt()
        if (version < v43) {
            reader += 24    //aabbMinDelta, aabbMaxDelta: Vector3
        }
        hasNormals = reader.readBool()
        hasTangents = reader.readBool()
        if (version >= v43) reader.alignStream()
    }
}

class MeshBlendShapeChannel internal constructor(reader: ObjectReader) {
    val name = reader.readAlignedString()
    val nameHash = reader.readUInt()
    val frameIndex = reader.readInt()
    val frameCount = reader.readInt()
}

class BlendShapeData internal constructor(reader: ObjectReader) {
    val vertices: Array<BlendShapeVertex>
    val shapes: Array<MeshBlendShape>
    val channels: Array<MeshBlendShapeChannel>
    val fullWeights: FloatArray

    init {
        if (reader.unityVersion >= intArrayOf(4, 3)) {
            vertices = reader.readArrayOf { BlendShapeVertex(reader) }
            shapes = reader.readArrayOf { MeshBlendShape(reader) }
            channels = reader.readArrayOf { MeshBlendShapeChannel(reader) }
            fullWeights = reader.readNextFloatArray()
        } else {
            reader.readArrayOf { MeshBlendShape(reader) }       //m_Shapes
            reader.alignStream()
            reader.readArrayOf { BlendShapeVertex(reader) }     //m_ShapeVertices
            vertices = emptyArray()
            shapes = emptyArray()
            channels = emptyArray()
            fullWeights = floatArrayOf()
        }
    }
}

class GfxPrimitiveType private constructor() {
    companion object {
        const val kPrimitiveTriangles = 0
        const val kPrimitiveTriangleStrip = 1
        const val kPrimitiveQuads = 2
    }
}

class SubMash internal constructor(reader: ObjectReader) {
    val firstByte = reader.readUInt()
    var indexCount = reader.readUInt()
        internal set
    val topology = reader.readInt()
    val triangleCount = if (reader.unityVersion[0] < 4) reader.readUInt() else 0u
    val baseVertex = if (reader.unityVersion >= intArrayOf(2017, 3)) reader.readInt() else 0
    val firstVertex: UInt
    val vertexCount: UInt
    val localAABB: AABB?

    init {
        if (reader.unityVersion[0] >= 3) {
            firstVertex = reader.readUInt()
            vertexCount = reader.readUInt()
            localAABB = AABB(reader)
        } else {
            firstVertex = 0u
            vertexCount = 0u
            localAABB = null
        }
    }
}