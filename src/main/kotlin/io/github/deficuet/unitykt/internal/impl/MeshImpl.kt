package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.enums.OrdinalEnumCompanion
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.util.*
import java.nio.ByteOrder
import kotlin.math.sqrt

internal class MeshImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Mesh, MeshFields(assetFile, info) {
    override val mSubMeshes: Array<SubMashImpl> get() {
        checkInitialize()
        return fmSubMeshes
    }
    override val mShapes: BlendShapeData? get() {
        checkInitialize()
        return fmShapes
    }
    override val mIndices: Array<UInt> get() {
        checkInitialize()
        return fmIndices
    }
    override val mBindPose: Array<Matrix4x4> get() {
        checkInitialize()
        return fmBindPose
    }
    override val mBoneNameHashes: Array<UInt> get() {
        checkInitialize()
        return fmBoneNameHashes
    }
    override val mVertexCount: Int get() {
        checkInitialize()
        return fmVertexCount
    }
    override val mVertices: FloatArray get() {
        checkInitialize()
        return fmVertices
    }
    override val mSkin: Array<BoneWeights4Impl> get() {
        checkInitialize()
        return fmSkin
    }
    override val mNormals: FloatArray get() {
        checkInitialize()
        return fmNormals
    }
    override val mColors: FloatArray get() {
        checkInitialize()
        return fmColors
    }
    override val mUV0: FloatArray get() {
        checkInitialize()
        return fmUV0
    }
    override val mUV1: FloatArray get() {
        checkInitialize()
        return fmUV1
    }
    override val mUV2: FloatArray get() {
        checkInitialize()
        return fmUV2
    }
    override val mUV3: FloatArray get() {
        checkInitialize()
        return fmUV3
    }
    override val mUV4: FloatArray get() {
        checkInitialize()
        return fmUV4
    }
    override val mUV5: FloatArray get() {
        checkInitialize()
        return fmUV5
    }
    override val mUV6: FloatArray get() {
        checkInitialize()
        return fmUV6
    }
    override val mUV7: FloatArray get() {
        checkInitialize()
        return fmUV7
    }
    override val mTangents: FloatArray get() {
        checkInitialize()
        return fmTangents
    }
    private var pmUse16BitIndices = true
    private var pmIndexBuffer: Array<UInt> = emptyArray()
    private lateinit var pmVertexData: VertexDataImpl
    private var pmCompressedMesh: CompressedMeshImpl? = null
    private var pmStreamData: StreamingInfoImpl? = null

    override fun read() {
        super.read()
        pmUse16BitIndices = if (unityVersion < intArrayOf(3, 5)) reader.readInt32() > 0 else true
        if (unityVersion <= intArrayOf(2, 5)) {
            val indexBufferSize = reader.readInt32()
            if (pmUse16BitIndices) {
                pmIndexBuffer = reader.readArrayOf(indexBufferSize / 2) { reader.readUInt16().toUInt() }
                reader.alignStream()
            } else {
                pmIndexBuffer = reader.readUInt32Array(indexBufferSize / 4)
            }
        }
        fmSubMeshes = reader.readArrayOf { SubMashImpl(this) }
        fmShapes = if (unityVersion >= intArrayOf(4, 1)) BlendShapeDataImpl(reader) else null
        if (unityVersion >= intArrayOf(4, 3)) {
            fmBindPose = reader.readMatrix4x4Array()
            fmBoneNameHashes = reader.readUInt32Array()
            reader.skip(4)     //m_RootBoneNameHash: UInt
        }
        if (unityVersion >= intArrayOf(2, 6)) {
            if (unityVersion[0] >= 2019) {
                reader.readArrayOf { MinMaxAABBImpl(this) }   //m_BonesAABB
                reader.readUInt32Array()      //m_VariableBoneCountWeights
            }
            val meshCompression = reader.readUInt8().toUInt()
            if (unityVersion[0] >= 4) {
                //m_StreamCompression: UByte, m_IsReadable, m_KeepVertices, m_KeepIndices: Boolean
                reader.skip(if (unityVersion[0] < 5) 4 else 3)
            }
            reader.alignStream()
            if (
                unityVersion >= intArrayOf(2017, 4) ||
                ((unityVersion[0] == 2017 && unityVersion[1] == 3 && unityVersion[2] == 1) && buildType.isPatch) ||
                ((unityVersion[0] == 2017 && unityVersion[1] == 3) && meshCompression == 0u)
            ) {
                pmUse16BitIndices = reader.readInt32() == 0
            }
            val indexBufferSize = reader.readInt32()
            if (pmUse16BitIndices) {
                pmIndexBuffer = reader.readArrayOf(indexBufferSize / 2) { reader.readUInt16().toUInt() }
                reader.alignStream()
            } else {
                pmIndexBuffer = reader.readUInt32Array(indexBufferSize / 4)
            }
        }
        if (unityVersion < intArrayOf(3, 5)) {
            fmVertexCount = reader.readInt32()
            fmVertices = reader.readFloatArray(fmVertexCount * 3)
            fmSkin = reader.readArrayOf { BoneWeights4Impl(this) }
            fmBindPose = reader.readMatrix4x4Array()
            fmUV0 = reader.readFloatArray(reader.readInt32() * 2)
            fmUV1 = reader.readFloatArray(reader.readInt32() * 2)
            if (unityVersion[0] == 2 && unityVersion[1] <= 5) {
                val tangentSpaceSize = reader.readInt32()
                fmNormals = FloatArray(tangentSpaceSize * 3)
                fmTangents = FloatArray(tangentSpaceSize * 4)
                for (v in 0 until tangentSpaceSize) {
                    fmNormals[v * 3] = reader.readFloat()
                    fmNormals[v * 3 + 1] = reader.readFloat()
                    fmNormals[v * 3 + 2] = reader.readFloat()
                    fmTangents[v * 3] = reader.readFloat()
                    fmTangents[v * 3 + 1] = reader.readFloat()
                    fmTangents[v * 3 + 2] = reader.readFloat()
                    fmTangents[v * 3 + 3] = reader.readFloat()
                }
            } else {
                fmNormals = reader.readFloatArray(reader.readInt32() * 3)
                fmTangents = reader.readFloatArray(reader.readInt32() * 4)
            }
        } else {
            if (unityVersion < intArrayOf(2018, 2)) {
                fmSkin = reader.readArrayOf { BoneWeights4Impl(this) }
            }
            if (unityVersion[0] == 3 || (unityVersion[0] == 4 && unityVersion[1] <= 2)) {
                fmBindPose = reader.readMatrix4x4Array()
            }
            pmVertexData = VertexDataImpl(reader)
        }
        if (unityVersion >= intArrayOf(2, 6)) {
            pmCompressedMesh =  CompressedMeshImpl(reader)
        }
        reader.skip(24)     //AABB m_LocalAABB
        if (unityVersion <= intArrayOf(3, 4)) {
            fmColors = FloatArray(reader.readInt32() * 4) {
                (reader.readUInt8() / 0xFFu).toFloat()
            }
            reader.skip(reader.readInt32() * 4 + 4)    //m_CollisionVertexCount
        }
        reader.skip(4)     //m_MeshUsageFlags: Int
        if (unityVersion >= intArrayOf(2022, 1)) {
            reader.skip(4)     //m_CookingOptions
        }
        if (unityVersion[0] >= 5) {
            reader.readInt8Array()      //m_BakedConvexCollisionMesh
            reader.alignStream()
            reader.readInt8Array()      //m_BakedTriangleCollisionMesh
            reader.alignStream()
        }
        if (unityVersion >= intArrayOf(2018, 2)) {
            reader.skip(8)     //m_MeshMetrics: float[2]
        }
        if (unityVersion >= intArrayOf(2018, 3)) {
            reader.alignStream()
            pmStreamData = StreamingInfoImpl(reader)
        }
        processData()
    }

    private fun processData() {
        val sd = pmStreamData
        if (sd?.path?.isNotEmpty() == true) {
            if (pmVertexData.mVertexCount > 0u) {
                ResourceReader(
                    sd.path, assetFile, sd.offset, sd.size.toLong()
                ).use {
                    pmVertexData.mDataSize = it.read()
                }
            }
        }
        if (unityVersion >= intArrayOf(3, 5)) {
            readVertexData()
        }
        if (unityVersion >= intArrayOf(2, 6)) {
            decompressMesh()
        }
        getTriangles()
    }

    private fun readVertexData() {
        fmVertexCount = pmVertexData.mVertexCount.toInt()
        for (chn in pmVertexData.mChannels.indices) {
            val channel = pmVertexData.mChannels[chn]
            if (channel.dimension > 0u) {
                val stream = pmVertexData.mStreams[channel.stream.toInt()]
                if (stream.channelMask.and(1u.shl(chn)) != 0u) {
                    if (unityVersion[0] < 2018 && chn == 2 && channel.format == 2u.toUByte()) {
                        channel.dimension = 4u
                    }
                    val vertexFormat = MeshHelper.toVertexFormat(channel.format, unityVersion)
                    val componentByteSize = vertexFormat.size.toInt()
                    val componentBytes = ByteArray(
                        fmVertexCount * channel.dimension.toInt() * componentByteSize
                    )
                    for (v in 0 until fmVertexCount) {
                        val vertexOffset = stream.offset.toInt() + channel.offset.toInt() + stream.stride.toInt() * v
                        for (d in 0 until channel.dimension.toInt()) {
                            val componentOffset = vertexOffset + componentByteSize * d
                            val buff = pmVertexData.mDataSize[componentOffset, componentByteSize]
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
                    var fa: FloatArray; var ia: IntArray
                    if (vertexFormat.isIntFormat) {
                        ia = componentBytes.toIntArray(vertexFormat)
                        fa = FloatArray(0)
                    } else {
                        fa = componentBytes.toFloatArray(vertexFormat)
                        ia = IntArray(0)
                    }
                    if (unityVersion[0] >= 2018) {
                        when (chn) {
                            0 -> fmVertices = fa
                            1 -> fmNormals = fa
                            2 -> fmTangents = fa
                            3 -> fmColors = fa
                            4 -> fmUV0 = fa
                            5 -> fmUV1 = fa
                            6 -> fmUV2 = fa
                            7 -> fmUV3 = fa
                            8 -> fmUV4 = fa
                            9 -> fmUV5 = fa
                            10 -> fmUV6 = fa
                            11 -> fmUV7 = fa
                            12 -> {
                                if (fmSkin.isEmpty()) {
                                    fmSkin = reader.readArrayOf(fmVertexCount) { BoneWeights4Impl() }
                                }
                                for (i in 0 until fmVertexCount) {
                                    for (j in 0 until channel.dimension.toInt()) {
                                        fmSkin[i].weight[j] = fa[i * channel.dimension.toInt() + j]
                                    }
                                }
                            }
                            13 -> {
                                if (fmSkin.isEmpty()) {
                                    fmSkin = reader.readArrayOf(fmVertexCount) { BoneWeights4Impl() }
                                }
                                for (i in 0 until fmVertexCount) {
                                    for (j in 0 until channel.dimension.toInt()) {
                                        fmSkin[i].boneIndex[j] = ia[i * channel.dimension.toInt() + j]
                                    }
                                }
                            }
                        }
                    } else {
                        when (chn) {
                            0 -> fmVertices = fa
                            1 -> fmNormals = fa
                            2 -> fmColors = fa
                            3 -> fmUV0 = fa
                            4 -> fmUV1 = fa
                            5 -> {
                                if (unityVersion[0] >= 5) {
                                    fmUV2 = fa
                                } else {
                                    fmTangents = fa
                                }
                            }
                            6 -> fmUV3 = fa
                            7 -> fmTangents = fa
                        }
                    }
                }
            }
        }
    }

    private fun decompressMesh() {
        val cm = pmCompressedMesh
        if (cm!!.mVertices.mNumItems > 0u) {
            fmVertexCount = (cm.mVertices.mNumItems / 3u).toInt()
            fmVertices = cm.mVertices.unpackFloats(3, 12)
        }
        if (cm.mUV.mNumItems > 0u) {
            val uvInfo = cm.mUVInfo
            if (uvInfo != 0u) {
                var uvSrcOffset = 0
                for (uv in 0 until kMaxTexCoordShaderChannels) {
                    var texCoordBits = uvInfo.shr(uv * kInfoBitsPerUV)
                    texCoordBits = texCoordBits.and(1u.shl(kInfoBitsPerUV) - 1u)
                    if (texCoordBits.and(kUVChannelExists) != 0u) {
                        val uvDim = 1 + texCoordBits.and(kUVDimensionMask).toInt()
                        val mUV = cm.mUV.unpackFloats(
                            uvDim, uvDim * 4, uvSrcOffset, fmVertexCount
                        )
                        //region setUV
                        when (uv) {
                            0 -> fmUV0 = mUV
                            1 -> fmUV1 = mUV
                            2 -> fmUV2 = mUV
                            3 -> fmUV3 = mUV
                            4 -> fmUV4 = mUV
                            5 -> fmUV5 = mUV
                            6 -> fmUV6 = mUV
                            7 -> fmUV7 = mUV
                            else -> throw IndexOutOfBoundsException()
                        }
                        //endregion
                        uvSrcOffset += uvDim * fmVertexCount
                    }
                }
            } else {
                fmUV0 = cm.mUV.unpackFloats(
                    2, 8, 0, fmVertexCount
                )
                if (cm.mUV.mNumItems >= (fmVertexCount * 4).toUInt()) {
                    fmUV1 = cm.mUV.unpackFloats(
                        2, 8, fmVertexCount * 2, fmVertexCount
                    )
                }
            }
            if (unityVersion[0] < 5 && cm.mBindPoses!!.mNumItems > 0u) {
                val unpackedBindPose = cm.mBindPoses.unpackFloats(16, 64)
                fmBindPose = reader.readArrayIndexedOf(
                    (cm.mBindPoses.mNumItems / 16u).toInt()
                ) {
                    Matrix4x4(
                        Array(4) { col ->
                            FloatArray(4) { row ->
                                unpackedBindPose[it * 16 + col * 4 + row]
                            }
                        }
                    )
                }
            }
            if (cm.mNormals.mNumItems > 0u) {
                val normalData = cm.mNormals.unpackFloats(2, 8)
                val signs = cm.mNormalSigns.unpackInts()
                fmNormals = FloatArray((cm.mNormals.mNumItems / 2u * 3u).toInt())
                for (i in 0 until (cm.mNormals.mNumItems / 2u).toInt()) {
                    var x = normalData[i * 2]
                    var y = normalData[i * 2 + 1]
                    val zsqr = 1 - x * x - y * y
                    var z: Float
                    if (zsqr >= 0f) {
                        z = sqrt(zsqr)
                    } else {
                        z = 0f
                        with(Vector2(x, y).unit) {
                            x = this.x
                            y = this.y
                        }
                    }
                    if (signs[i] == 0) z = -z
                    fmNormals[i * 3] = x
                    fmNormals[i * 3 + 1] = y
                    fmNormals[i * 3 + 2] = z
                }
            }
            if (cm.mTangents.mNumItems > 0u) {
                val tangentData = cm.mTangents.unpackFloats(2, 8)
                val signs = cm.mTangentSigns.unpackInts()
                fmTangents = FloatArray((cm.mTangents.mNumItems / 2u * 4u).toInt())
                for (i in 0 until (cm.mTangents.mNumItems / 2u).toInt()) {
                    var x = tangentData[i * 2]
                    var y = tangentData[i * 2 + 1]
                    val zsqr = 1 - x * x - y * y
                    var z: Float
                    if (zsqr >= 0f) {
                        z = sqrt(zsqr)
                    } else {
                        z = 0f
                        with(Vector2(x, y).unit) {
                            x = this.x
                            y = this.y
                        }
                    }
                    if (signs[i * 2] == 0) z = -z
                    val w = if (signs[i * 2 + 1] > 0) 1f else -1f
                    fmTangents[i * 4] = x
                    fmTangents[i * 4 + 1] = y
                    fmTangents[i * 4 + 2] = z
                    fmTangents[i * 4 + 3] = w
                }
            }
            if (unityVersion[0] >= 5) {
                if (cm.mFloatColors!!.mNumItems > 0u) {
                    fmColors = cm.mFloatColors.unpackFloats(1, 4)
                }
            }
            if (cm.mWeights.mNumItems > 0u) {
                val weights = cm.mWeights.unpackInts()
                val boneIndices = cm.mBoneIndices.unpackInts()
                fmSkin = reader.readArrayOf(fmVertexCount) { BoneWeights4Impl() }
                var bonePos = 0; var boneIndexPos = 0; var j = 0; var sum = 0
                for (i in 0 until cm.mWeights.mNumItems.toInt()) {
                    with(fmSkin[bonePos]) {
                        weight[j] = weights[i] / 31f
                        boneIndex[j] = boneIndices[boneIndexPos++]
                    }
                    j++; sum += weights[i]
                    if (sum >= 31) {
                        while (j < 4) {
                            with(fmSkin[bonePos]) {
                                weight[j] = 0f
                                boneIndex[j] = 0
                            }
                            j++
                        }
                        bonePos++
                        j = 0; sum = 0
                    } else if (j == 3) {
                        with(fmSkin[bonePos]) {
                            weight[j] = (31 - sum) / 31f
                            boneIndex[j] = boneIndices[boneIndexPos++]
                        }
                        bonePos++
                        j = 0; sum = 0
                    }
                }
            }
            if (cm.mTriangles.mNumItems > 0u) {
                pmIndexBuffer = with(cm.mTriangles.unpackInts()) { Array(size) { this[it].toUInt() } }
            }
            if (with(cm.mColors) { this != null && mNumItems > 0u }) {
                cm.mColors!!.mNumItems *= 4u
                cm.mColors!!.mBitSize = (cm.mColors.mBitSize / 4u).toUByte()
                val tempColors = cm.mColors.unpackInts()
                fmColors = FloatArray(cm.mColors.mNumItems.toInt()) {
                    tempColors[it] / 255f
                }
            }
        }
    }

    private fun getTriangles() {
        val indices = mutableListOf<UInt>()
        for (subMesh in fmSubMeshes) {
            var firstIdx = (subMesh.firstByte / 2u).toInt()
            if (!pmUse16BitIndices) {
                firstIdx /= 2
            }
            val indexCount = subMesh.indexCount.toInt()
            if (subMesh.topology == GfxPrimitiveType.Triangles) {
                for (i in 0 until indexCount step 3) {
                    with(indices) {
                        add(pmIndexBuffer[firstIdx + i])
                        add(pmIndexBuffer[firstIdx + i + 1])
                        add(pmIndexBuffer[firstIdx + i + 2])
                    }
                }
            } else if (unityVersion[0] < 4 || subMesh.topology == GfxPrimitiveType.TriangleStrip) {
                var triIndex = 0u
                for (i in 0 until indexCount - 2) {
                    val a = pmIndexBuffer[firstIdx + i]
                    val b = pmIndexBuffer[firstIdx + i + 1]
                    val c = pmIndexBuffer[firstIdx + i + 2]
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
                        indices.add(pmIndexBuffer[firstIdx + q + x])
                    }
                }
                subMesh.indexCount = (indexCount / 2 * 3).toUInt()
            } else {
                throw IllegalStateException("Failed getting triangles. Submesh topology is lines or points.")
            }
        }
        fmIndices = indices.toTypedArray()
    }

    private fun ByteArray.toIntArray(vertexFormat: VertexFormat): IntArray {
        val len = size / vertexFormat.size.toInt()
        return IntArray(len) {
            val i = it.toUInt()
            when (vertexFormat) {
                VertexFormat.UInt8,
                VertexFormat.SInt8 -> this[i]
                VertexFormat.UInt16,
                VertexFormat.SInt16 -> parseUInt16(this, i * 2u).toShort().toInt()
                VertexFormat.UInt32,
                VertexFormat.SInt32 -> parseInt(this, i * 4u)
                else -> { 0 }
            }
        }
    }

    private fun ByteArray.toFloatArray(vertexFormat: VertexFormat): FloatArray {
        val len = size / vertexFormat.size.toInt()
        return FloatArray(len)  {
            val i = it.toUInt()
            when (vertexFormat) {
                VertexFormat.Float -> parseFloat(this, i * 4u)
                VertexFormat.Float16 -> {
                    parseHalf(this, i * 2u)
                }
                VertexFormat.UNorm8 -> {
                    this[i] / 255f
                }
                VertexFormat.SNorm8 -> {
                    maxOf(this[it] / 127f, -1f)
                }
                VertexFormat.UNorm16 -> {
                    parseUInt16(this, i * 2u) / 65536f
                }
                VertexFormat.SNorm16 -> {
                    maxOf(parseUInt16(this, i * 2u).toShort() / 32767f, -1f)
                }
                else -> { 0f }
            }
        }
    }

    override fun exportString(): String {
        if (mVertexCount < 0) return ""
        val builder = StringBuilder()
        builder.append("g $mName\r\n")
        if (mVertices.isEmpty()) return ""
        var c = if (mVertices.size == mVertexCount * 4) 4 else 3
        for (v in 0 until mVertexCount) {
            builder.append("v ${-mVertices[v * c]} ${mVertices[v * c + 1]} ${mVertices[v * c + 2]}\r\n")
        }
        if (mUV0.isNotEmpty()) {
            c = when (mUV0.size) {
                mVertexCount * 2 -> 2
                mVertexCount * 3 -> 3
                else -> 4
            }
            for (vt in 0 until mVertexCount) {
                builder.append("vt ${mUV0[vt * c]} ${mUV0[vt * c + 1]}\r\n")
            }
        }
        if (mNormals.isNotEmpty()) {
            when(mNormals.size) {
                mVertexCount * 3 -> c = 3
                mVertexCount * 4 -> c = 4
            }
            for (vn in 0 until mVertexCount) {
                builder.append("vn ${-mNormals[vn * c]} ${mNormals[vn * c + 1]} ${mNormals[vn * c + 2]}\r\n")
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

    override fun exportVertices(): Array<out Vector3> {
        val c = if (mVertices.size == mVertexCount * 4) 4 else 3
        return Array(mVertexCount) { Vector3(mVertices[it * c], mVertices[it * c + 1], mVertices[it * c + 2]) }
    }

    override fun exportUV(): Array<out Vector2> {
        return if (mUV0.isEmpty()) {
            emptyArray()
        } else {
            val c = when (mUV0.size) {
                mVertexCount * 2 -> 2
                mVertexCount * 3 -> 3
                else -> 4
            }
            Array(mVertexCount) { Vector2(mUV0[it * c], mUV0[it * c + 1]) }
        }
    }

    override fun exportNormals(): Array<out Vector3> {
        return if (mNormals.isEmpty()) {
            emptyArray()
        } else {
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

    override fun exportFaces(): Array<out Array<out Vector3>> {
        var sum = 0
        return Array(mSubMeshes.size) { i ->
            val end = sum + mSubMeshes[i].indexCount.toInt() / 3
            val a = Array(end - sum) { f ->
                Vector3(
                    (mIndices[(f + sum) * 3 + 2] + 1u).toFloat(),
                    (mIndices[(f + sum) * 3 + 1] + 1u).toFloat(),
                    (mIndices[(f + sum) * 3] + 1u).toFloat()
                )
            }
            sum = end
            a
        }
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
            *Array(VertexFormat2017.array.size) {
                when(val vf2017 = VertexFormat2017.of(it)) {
                    VertexFormat2017.Color -> vf2017 to VertexFormat.UNorm8
                    else -> vf2017 to VertexFormat.valueOf(vf2017.name)
                }
            }
        )

        internal fun toVertexFormat(i: UByte, version: IntArray): VertexFormat {
            return if (version[0] < 2017) {
                val vcf = VertexChannelFormat.of(i.toInt())
                vertexFormatMap[vcf] ?: throw NoSuchElementException(vcf.name)
            } else if (version[0] < 2019) {
                val vf2017 = VertexFormat2017.of(i.toInt())
                vertexFormat2017Map[vf2017] ?: throw NoSuchElementException(vf2017.name)
            } else {
                VertexFormat.of(i.toInt())
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

    companion object: OrdinalEnumCompanion<VertexChannelFormat>(values())
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

    companion object: OrdinalEnumCompanion<VertexFormat2017>(values())
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

    companion object: OrdinalEnumCompanion<VertexFormat>(values())
}

internal class MinMaxAABBImpl(reader: EndianBinaryReader): MinMaxAABB {
    override val mMin = reader.readVector3()
    override val mMax = reader.readVector3()
}

internal class CompressedMeshImpl(reader: ObjectReader): CompressedMesh {
    override val mVertices = PackedFloatVectorImpl(reader)
    override val mUV = PackedFloatVectorImpl(reader)
    override val mBindPoses = if (reader.unityVersion[0] < 5) PackedFloatVectorImpl(reader) else null
    override val mNormals = PackedFloatVectorImpl(reader)
    override val mTangents = PackedFloatVectorImpl(reader)
    override val mWeights = PackedIntVectorImpl(reader)
    override val mNormalSigns = PackedIntVectorImpl(reader)
    override val mTangentSigns = PackedIntVectorImpl(reader)
    override val mFloatColors = if (reader.unityVersion[0] >= 5) PackedFloatVectorImpl(reader) else null
    override val mBoneIndices = PackedIntVectorImpl(reader)
    override val mTriangles = PackedIntVectorImpl(reader)
    override val mColors: PackedIntVectorImpl?
    override val mUVInfo: UInt

    init {
        if (reader.unityVersion >= intArrayOf(3, 5)) {
            if (reader.unityVersion[0] < 5) {
                mColors = PackedIntVectorImpl(reader)
                mUVInfo = 0u
            } else {
                mUVInfo = reader.readUInt32()
                mColors = null
            }
        } else {
            mUVInfo = 0u
            mColors = null
        }
    }
}

internal class StreamInfoImpl: StreamInfo {
    override val channelMask: UInt
    override val offset: UInt
    override val stride: UInt
    override val align: UInt
    override val dividerOp: UByte
    override val frequency: UShort

    internal constructor(reader: ObjectReader) {
        channelMask = reader.readUInt32()
        offset = reader.readUInt32()
        if (reader.unityVersion[0] < 4) {
            stride = reader.readUInt32()
            align  = reader.readUInt32()
            dividerOp = 0u
            frequency = 0u
        } else {
            stride = reader.readUInt8().toUInt()
            dividerOp = reader.readUInt8()
            frequency = reader.readUInt16()
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

internal class ChannelInfoImpl: ChannelInfo {
    override var stream: UByte
        internal set
    override var offset: UByte
        internal set
    override var format: UByte
        internal set
    override var dimension: UByte
        internal set

    constructor(reader: ObjectReader) {
        stream = reader.readUInt8()
        offset = reader.readUInt8()
        format = reader.readUInt8()
        dimension = reader.readUInt8().and(0xfu)
    }

    constructor() {
        stream = 0u
        offset = 0u
        format = 0u
        dimension = 0u
    }
}

internal class VertexDataImpl(reader: ObjectReader): VertexData {
    override val mCurrentChannels = if (reader.unityVersion[0] < 2018) reader.readUInt32() else 0u
    override val mVertexCount = reader.readUInt32()
    override val mChannels: Array<ChannelInfoImpl>
    override val mStreams: Array<StreamInfoImpl>
    override lateinit var mDataSize: ByteArray
        internal set

    init {
        val version = reader.unityVersion
        var channels = emptyArray<ChannelInfoImpl>()
        if (version[0] >= 4) {
            channels = reader.readArrayOf { ChannelInfoImpl(this) }
        }
        if (version[0] < 5) {
            val streamSize = if (version[0] < 4) 4 else reader.readInt32()
            mStreams = reader.readArrayOf(streamSize) { StreamInfoImpl(this) }
            if (version[0] < 4) {
                //region getChannels
                channels = Array(6) { ChannelInfoImpl() }
                for (s in mStreams.indices) {
                    val stream = mStreams[s]
//                    val channelMask = BitSet.valueOf(longArrayOf(stream.channelMask.toLong()))
                    var offset: UByte = 0u
                    for (j in 0..5) {
                        if (stream.channelMask.and(1u.shl(j)) != 0u) {
                            val channel = channels[j]
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
                            val inc = channel.dimension * MeshHelper.toVertexFormat(channel.format, version).size
                            offset = (offset + inc).toUByte()
                        }
                    }
                }
                //endregion
            }
        } else {
            //region getStream
            var offset = 0u
            mStreams = Array(channels.maxOf { it.stream }.toInt() + 1) {
                var chnMask = 0u
                var stride = 0u
                for (chn in channels.indices) {
                    val channel = channels[chn]
                    if (channel.stream.toInt() == it && channel.dimension > 0u) {
                        chnMask = chnMask.or(1u.shl(chn))
                        stride += channel.dimension * MeshHelper.toVertexFormat(channel.format, version).size
                    }
                }
                val stream = StreamInfoImpl(
                    chnMask, offset, stride, 0u, 0u
                )
                offset += mVertexCount * stride
                offset = (offset + 15u).and(15u.inv())
                stream
            }
            //endregion
        }
        mDataSize = reader.readInt8Array()
        mChannels = channels
    }
}

internal class BoneWeights4Impl: BoneWeights4 {
    override val weight: FloatArray
    override val boneIndex: IntArray

    constructor(reader: ObjectReader) {
        weight = reader.readFloatArray(4)
        boneIndex = reader.readInt32Array(4)
    }

    constructor() {
        weight = FloatArray(4)
        boneIndex = IntArray(4)
    }
}

internal class BlendShapeVertexImpl(reader: ObjectReader): BlendShapeVertex {
    override val vertex = reader.readVector3()
    override val normal = reader.readVector3()
    override val tangent = reader.readVector3()
    override val index = reader.readUInt32()
}

internal class MeshBlendShapeImpl(reader: ObjectReader): MeshBlendShape {
    override val firstVertex: UInt
    override val vertexCount: UInt
    override val hasNormals: Boolean
    override val hasTangents: Boolean

    init {
        val version = reader.unityVersion
        val v43 = intArrayOf(4, 3)
        if (version < v43) {
            reader.readAlignedString()      //name
        }
        firstVertex = reader.readUInt32()
        vertexCount = reader.readUInt32()
        if (version < v43) {
            reader.skip(24)    //aabbMinDelta, aabbMaxDelta: Vector3
        }
        hasNormals = reader.readBool()
        hasTangents = reader.readBool()
        if (version >= v43) reader.alignStream()
    }
}

internal class MeshBlendShapeChannelImpl(reader: ObjectReader): MeshBlendShapeChannel {
    override val name = reader.readAlignedString()
    override val nameHash = reader.readUInt32()
    override val frameIndex = reader.readInt32()
    override val frameCount = reader.readInt32()
}

internal class BlendShapeDataImpl(reader: ObjectReader): BlendShapeData {
    override val vertices: Array<BlendShapeVertexImpl>
    override val shapes: Array<MeshBlendShapeImpl>
    override val channels: Array<MeshBlendShapeChannelImpl>
    override val fullWeights: FloatArray

    init {
        if (reader.unityVersion >= intArrayOf(4, 3)) {
            vertices = reader.readArrayOf { BlendShapeVertexImpl(this) }
            shapes = reader.readArrayOf { MeshBlendShapeImpl(this) }
            channels = reader.readArrayOf { MeshBlendShapeChannelImpl(this) }
            fullWeights = reader.readFloatArray()
        } else {
            reader.readArrayOf { MeshBlendShapeImpl(this) }       //m_Shapes
            reader.alignStream()
            reader.readArrayOf { BlendShapeVertexImpl(this) }     //m_ShapeVertices
            vertices = emptyArray()
            shapes = emptyArray()
            channels = emptyArray()
            fullWeights = floatArrayOf()
        }
    }
}

internal class GfxPrimitiveType private constructor() {
    companion object {
        const val Triangles = 0
        const val TriangleStrip = 1
        const val Quads = 2
    }
}

internal class SubMashImpl(reader: ObjectReader): SubMash {
    override val firstByte = reader.readUInt32()
    override var indexCount = reader.readUInt32()
        internal set
    override val topology = reader.readInt32()
    override val triangleCount = if (reader.unityVersion[0] < 4) reader.readUInt32() else 0u
    override val baseVertex = if (reader.unityVersion >= intArrayOf(2017, 3)) reader.readUInt32() else 0u
    override val firstVertex: UInt
    override val vertexCount: UInt
    override val localAABB: AABBImpl?

    init {
        if (reader.unityVersion[0] >= 3) {
            firstVertex = reader.readUInt32()
            vertexCount = reader.readUInt32()
            localAABB = AABBImpl(reader)
        } else {
            firstVertex = 0u
            vertexCount = 0u
            localAABB = null
        }
    }
}
