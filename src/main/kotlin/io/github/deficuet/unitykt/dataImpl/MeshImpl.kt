package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.dataImpl.MeshHelper.Companion.toVertexFormat
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.sqrt

class MeshImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
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
        if (unityVersion >= intArrayOf(2022, 1)) {
            reader += 4     //m_CookingOptions
        }
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
                ResourceReader(
                    mStreamData.path, assetFile, mStreamData.offset, mStreamData.size.toLong()
                ).use {
                    mVertexData.mDataSize = it.read()
                }
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
                            val vertexOffset = stream.offset.toInt() +
                                    channel.offset.toInt() +
                                    stream.stride.toInt() * v
                            for (d in 0 until channel.dimension.toInt()) {
                                val componentOffset = vertexOffset + componentByteSize * d
                                val buff = mVertexData.mDataSize[componentOffset, componentByteSize]
                                if (reader.endian == ByteOrder.LITTLE_ENDIAN && componentByteSize > 1) {
                                    buff.reverse()
                                }
                                System.arraycopy(
                                    buff, 0, componentBytes,
                                    componentByteSize * (channel.dimension.toInt() * v + d),
                                    componentByteSize
                                )
                            }
                        }
                        val fa: FloatArray?
                        val ia: IntArray?
                        if (vertexFormat.isIntFormat) {
                            ia = componentBytes.toIntArray(vertexFormat)
                            fa = null
                        } else {
                            fa = componentBytes.toFloatArray(vertexFormat)
                            ia = null
                        }
                        if (unityVersion[0] >= 2018) {
                            when (chn) {
                                0 -> mVertices = fa!!
                                1 -> mNormals = fa!!
                                2 -> mTangents = fa!!
                                3 -> mColors = fa!!
                                4 -> mUV0 = fa!!
                                5 -> mUV1 = fa!!
                                6 -> mUV2 = fa!!
                                7 -> mUV3 = fa!!
                                8 -> mUV4 = fa!!
                                9 -> mUV5 = fa!!
                                10 -> mUV6 = fa!!
                                11 -> mUV7 = fa!!
                                12 -> {
                                    if (mSkin.isEmpty()) {
                                        mSkin = reader.readArrayOf(mVertexCount) { BoneWeights4() }
                                    }
                                    for (i in 0 until mVertexCount) {
                                        for (j in 0 until channel.dimension.toInt()) {
                                            mSkin[i].weight[j] = fa!![i * channel.dimension.toInt() + j]
                                        }
                                    }
                                }
                                13 -> {
                                    if (mSkin.isEmpty()) {
                                        mSkin = reader.readArrayOf(mVertexCount) { BoneWeights4() }
                                    }
                                    for (i in 0 until mVertexCount) {
                                        for (j in 0 until channel.dimension.toInt()) {
                                            mSkin[i].boneIndex[j] = ia!![i * channel.dimension.toInt() + j]
                                        }
                                    }
                                }
                            }
                        } else {
                            when (chn) {
                                0 -> mVertices = fa!!
                                1 -> mNormals = fa!!
                                2 -> mColors = fa!!
                                3 -> mUV0 = fa!!
                                4 -> mUV1 = fa!!
                                5 -> {
                                    if (unityVersion[0] >= 5) {
                                        mUV2 = fa!!
                                    } else {
                                        mTangents = fa!!
                                    }
                                }
                                6 -> mUV3 = fa!!
                                7 -> mTangents = fa!!
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
                        with(Vector3(x, y, z).unit) {
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
                        with(Vector3(x, y, z).unit) {
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
            if (subMesh.topology == GfxPrimitiveType.Triangles) {
                for (i in 0 until indexCount step 3) {
                    with(indices) {
                        add(mIndexBuffer[firstIdx + i])
                        add(mIndexBuffer[firstIdx + i + 1])
                        add(mIndexBuffer[firstIdx + i + 2])
                    }
                }
            } else if (unityVersion[0] < 4 && subMesh.topology == GfxPrimitiveType.TriangleStrip) {
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
            } else if (subMesh.topology == GfxPrimitiveType.Quads) {
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

    val exportString: String
        get() {
            if (mVertexCount < 0) return ""
            val builder = StringBuilder()
            builder.append("g $mName\r\n")
            if (mVertices.isEmpty()) return ""
            var c = if (mVertices.size == mVertexCount * 4) 4 else 3
            for (v in 0 until mVertexCount) {
                builder.append("v ${"%.7G"(-mVertices[v * c])} " +
                        "${"%.7G"(mVertices[v * c + 1])} " +
                        "${"%.7G"(mVertices[v * c + 2])}\r\n")
            }
            if (mUV0.isNotEmpty()) {
                c = when (mUV0.size) {
                    mVertexCount * 2 -> 2
                    mVertexCount * 3 -> 3
                    else -> 4
                }
                for (vt in 0 until mVertexCount) {
                    builder.append("vt ${"%.7G"(mUV0[vt * c]).trimEnd('0')} " +
                            "${"%.7G"(mUV0[vt * c + 1]).trimEnd('0')}\r\n")
                }
            }
            if (mNormals.isNotEmpty()) {
                when(mNormals.size) {
                    mVertexCount * 3 -> c = 3
                    mVertexCount * 4 -> c = 4
                }
                for (vn in 0 until mVertexCount) {
                    builder.append("vn ${"%.7G"(-mNormals[vn * c])} " +
                            "${"%.7G"(mNormals[vn * c + 1])} " +
                            "${"%.7G"(mNormals[vn * c + 2])}\r\n")
                }
            }
            var sum = 0
            for (i in mSubMeshes.indices) {
                builder.append("g ${mName}_$i\r\n")
                val end = sum + mSubMeshes[i].indexCount.toInt() / 3
                for (f in sum until end) {
                    val v0 = mIndices[f * 3 + 2] + 1u
                    val v1 = mIndices[f * 3 + 1] + 1u
                    val v2 = mIndices[f * 3] + 1u
                    builder.append("f $v0/$v0/$v0 $v1/$v1/$v1 $v2/$v2/$v2\r\n")
                }
                sum = end
            }
            return builder.toString().replace("NaN", "0")
        }

    val exportVertices: Array<Vector3>
        get() {
            val c = if (mVertices.size == mVertexCount * 4) 4 else 3
            return Array(mVertexCount) { Vector3(mVertices[it * c], mVertices[it * c + 1], mVertices[it * c + 2]) }
        }

    val exportUV: Array<Vector2>
        get() {
            return if (mUV0.isEmpty()) arrayOf()
            else {
                val c = when (mUV0.size) {
                    mVertexCount * 2 -> 2
                    mVertexCount * 3 -> 3
                    else -> 4
                }
                Array(mVertexCount) { Vector2(mUV0[it * c], mUV0[it * c + 1]) }
            }
        }

    val exportNormals: Array<Vector3>
        get() {
            return if (mNormals.isEmpty()) arrayOf()
            else {
                var c = when (mUV0.size) {
                    mVertexCount * 2 -> 2
                    mVertexCount * 3 -> 3
                    else -> 4
                }
                when(mNormals.size) {
                    mVertexCount * 3 -> c = 3
                    mVertexCount * 4 -> c = 4
                }
                Array(mVertexCount) { Vector3(mNormals[it * c], mNormals[it * c + 1], mNormals[it * c + 2]) }
            }
        }

    val exportFaces: Array<Array<Vector3>>
        get() {
            var sum = 0
            return Array(mSubMeshes.size) {
                val end = sum + mSubMeshes[it].indexCount.toInt() / 3
                val v = mutableListOf<Vector3>()
                for (f in sum until end) {
                    v.add(Vector3(
                        (mIndices[f * 3 + 2] + 1u).toDouble(),
                        (mIndices[f * 3 + 1] + 1u).toDouble(),
                        (mIndices[f * 3] + 1u).toDouble()
                    ))
                }
                sum = end
                v.toTypedArray()
            }
        }

    private fun ByteArray.toIntArray(vertexFormat: VertexFormat): IntArray {
        val len = size / vertexFormat.size.toInt()
        val result = IntArray(len)
        for (i in 0 until len) {
            when (vertexFormat) {
                VertexFormat.UInt8,
                VertexFormat.SInt8 -> result[i] = this[i].toIntBits()
                VertexFormat.UInt16,
                VertexFormat.SInt16 -> {
                    result[i] = ByteBuffer.wrap(this[i * 2, 2]).short.toInt()
                }
                VertexFormat.UInt32,
                VertexFormat.SInt32 -> {
                    result[i] = ByteBuffer.wrap(this[i * 4, 4]).int
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
                VertexFormat.Float -> {
                    result[i] = ByteBuffer.wrap(this[i * 4, 4]).float
                }
                VertexFormat.Float16 -> {
                    result[i] = this[i * 2, 2].toHalf()
                }
                VertexFormat.UNorm8 -> {
                    result[i] = maxOf(get(i) / 127f, -1f)
                }
                VertexFormat.SNorm8 -> {
                    result[i] = ByteBuffer.wrap(this[i * 2, 2]).short / 65536f
                }
                VertexFormat.SNorm16 -> {
                    result[i] = maxOf(
                        ByteBuffer.wrap(this[i * 2, 2]).short / 32767f,
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
            VertexChannelFormat.Float to VertexFormat.Float,
            VertexChannelFormat.Float16 to VertexFormat.Float16,
            VertexChannelFormat.Color to VertexFormat.UNorm8,
            VertexChannelFormat.Byte to VertexFormat.UInt8,
            VertexChannelFormat.UInt32 to VertexFormat.UInt32
        )
        private val vertexFormat2017Map = mapOf(
            *(VertexFormat.values().map { vf ->
                VertexFormat2017.valueOf(vf.name) to vf
            } + listOf(VertexFormat2017.Color to VertexFormat.UNorm8))
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

internal enum class VertexChannelFormat {
    Float,
    Float16,
    Color,
    Byte,
    UInt32;

    companion object {
        fun of(value: Int): VertexChannelFormat {
            return values()[value]
        }
    }
}

internal enum class VertexFormat2017 {
    Float,
    Float16,
    Color,
    UNorm8,
    SNorm8,
    UNorm16,
    SNorm16,
    UInt8,
    SInt8,
    UInt16,
    SInt16,
    UInt32,
    SInt32;

    companion object {
        fun of(value: Int): VertexFormat2017 {
            return values()[value]
        }
    }
}

internal enum class VertexFormat(val size: UInt) {
    Float(4u),
    Float16(2u),
    UNorm8(1u),
    SNorm8(1u),
    UNorm16(2u),
    SNorm16(2u),
    UInt8(1u),
    SInt8(1u),
    UInt16(2u),
    SInt16(2u),
    UInt32(4u),
    SInt32(4u);

    val isIntFormat get() = this >= UInt8

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
        const val Triangles = 0
        const val TriangleStrip = 1
        const val Quads = 2
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