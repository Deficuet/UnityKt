package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector3

interface Mesh: NamedObject {
    val mSubMeshes: Array<out SubMash>
    val mShapes: BlendShapeData?
    val mIndices: Array<out UInt>
    val mBindPose: Array<out Matrix4x4>
    val mBoneNameHashes: Array<out UInt>
    val mVertexCount: Int
    val mVertices: FloatArray
    val mSkin: Array<out BoneWeights4>
    val mNormals: FloatArray
    val mColors: FloatArray
    val mUV0: FloatArray
    val mUV1: FloatArray
    val mUV2: FloatArray
    val mUV3: FloatArray
    val mUV4: FloatArray
    val mUV5: FloatArray
    val mUV6: FloatArray
    val mUV7: FloatArray
    val mTangents: FloatArray

    fun exportString(): String

    /**
     * The data for lines starting with "v"
     */
    fun exportVertices(): Array<out Vector3>

    /**
     * The data for lines starting with "vt"
     */
    fun exportUV(): Array<out Vector2>

    /**
     * The data for lines starting with "vn"
     */
    fun exportNormals(): Array<out Vector3>

    /**
     * The data for lines starting with "f"
     */
    fun exportFaces(): Array<out Array<out Vector3>>
}

interface MinMaxAABB {
    val mMin: Vector3
    val mMax: Vector3
}

interface CompressedMesh {
    val mVertices: PackedFloatVector
    val mUV: PackedFloatVector
    val mBindPoses: PackedFloatVector?
    val mNormals: PackedFloatVector
    val mTangents: PackedFloatVector
    val mWeights: PackedIntVector
    val mNormalSigns: PackedIntVector
    val mTangentSigns: PackedIntVector
    val mFloatColors: PackedFloatVector?
    val mBoneIndices: PackedIntVector
    val mTriangles: PackedIntVector
    val mColors: PackedIntVector?
    val mUVInfo: UInt
}

interface StreamInfo {
    val channelMask: UInt
    val offset: UInt
    val stride: UInt
    val align: UInt
    val dividerOp: UByte
    val frequency: UShort
}

interface ChannelInfo {
    val stream: UByte
    val offset: UByte
    val format: UByte
    val dimension: UByte
}

interface VertexData {
    val mCurrentChannels: UInt
    val mVertexCount: UInt
    val mChannels: Array<out ChannelInfo>
    val mStreams: Array<out StreamInfo>
    val mDataSize: ByteArray
}

interface BoneWeights4 {
    val weight: FloatArray
    val boneIndex: IntArray
}

interface BlendShapeVertex {
    val vertex: Vector3
    val normal: Vector3
    val tangent: Vector3
    val index: UInt
}

interface MeshBlendShape {
    val firstVertex: UInt
    val vertexCount: UInt
    val hasNormals: Boolean
    val hasTangents: Boolean
}

interface MeshBlendShapeChannel {
    val name: String
    val nameHash: UInt
    val frameIndex: Int
    val frameCount: Int
}

interface BlendShapeData {
    val vertices: Array<out BlendShapeVertex>
    val shapes: Array<out MeshBlendShape>
    val channels: Array<out MeshBlendShapeChannel>
    val fullWeights: FloatArray
}

interface SubMash {
    val firstByte: UInt
    val indexCount: UInt
    val topology: Int
    val triangleCount: UInt
    val baseVertex: UInt
    val firstVertex: UInt
    val vertexCount: UInt
    val localAABB: AABB?
}
