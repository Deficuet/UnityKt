package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MeshImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Mesh private constructor(
    private val container: ImplementationContainer<MeshImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MeshImpl(ObjectReader(assetFile, info)) })

    val mSubMeshes get() = container.impl.mSubMeshes
    val mShapes get() = container.impl.mShapes
    val mIndices get() = container.impl.mIndices
    val mBindPose get() = container.impl.mBindPose
    val mBoneNameHashes get() = container.impl.mBoneNameHashes
    val mVertexCount get() = container.impl.mVertexCount
    val mVertices get() = container.impl.mVertices
    val mSkin get() = container.impl.mSkin
    val mNormals get() = container.impl.mNormals
    val mColors get() = container.impl.mColors
    val mUV0 get() = container.impl.mUV0
    val mUV1 get() = container.impl.mUV1
    val mUV2 get() = container.impl.mUV2
    val mUV3 get() = container.impl.mUV3
    val mUV4 get() = container.impl.mUV4
    val mUV5 get() = container.impl.mUV5
    val mUV6 get() = container.impl.mUV6
    val mUV7 get() = container.impl.mUV7
    val mTangents get() = container.impl.mTangents
}