package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.BlendShapeData
import io.github.deficuet.unitykt.dataImpl.BoneWeights4
import io.github.deficuet.unitykt.dataImpl.MeshImpl
import io.github.deficuet.unitykt.dataImpl.SubMash
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.util.ObjectReader

class Mesh private constructor(
    private val container: ImplementationContainer<MeshImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MeshImpl(ObjectReader(assetFile, info)) })

    val mSubMeshes: Array<SubMash>          get() = container.impl.mSubMeshes
    val mShapes: BlendShapeData?            get() = container.impl.mShapes
    val mIndices: Array<UInt>               get() = container.impl.mIndices
    val mBindPose: Array<Matrix4x4>         get() = container.impl.mBindPose
    val mBoneNameHashes: Array<UInt>        get() = container.impl.mBoneNameHashes
    val mVertexCount: Int                   get() = container.impl.mVertexCount
    val mVertices: FloatArray               get() = container.impl.mVertices
    val mSkin: Array<BoneWeights4>          get() = container.impl.mSkin
    val mNormals: FloatArray                get() = container.impl.mNormals
    val mColors: FloatArray                 get() = container.impl.mColors
    val mUV0: FloatArray                    get() = container.impl.mUV0
    val mUV1: FloatArray                    get() = container.impl.mUV1
    val mUV2: FloatArray                    get() = container.impl.mUV2
    val mUV3: FloatArray                    get() = container.impl.mUV3
    val mUV4: FloatArray                    get() = container.impl.mUV4
    val mUV5: FloatArray                    get() = container.impl.mUV5
    val mUV6: FloatArray                    get() = container.impl.mUV6
    val mUV7: FloatArray                    get() = container.impl.mUV7
    val mTangents: FloatArray               get() = container.impl.mTangents

    /**
     * Same content to the export .obj file.
     */
    val exportString: String                get() = container.impl.exportString

    /**
     * The data of lines start with "v"
     */
    val exportVertices: Array<Vector3>      get() = container.impl.exportVertices

    /**
     * The data of lines start with "vt"
     */
    val exportUV: Array<Vector2>            get() = container.impl.exportUV

    /**
     * The data of lines start with "vn"
     */
    val exportNormals: Array<Vector3>       get() = container.impl.exportNormals

    /**
     * The data of lines start with "f"
     */
    val exportFaces: Array<Array<Vector3>>  get() = container.impl.exportFaces
}