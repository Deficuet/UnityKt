package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.SkinnedMeshRendererImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class SkinnedMeshRenderer private constructor(
    private val container: ImplementationContainer<SkinnedMeshRendererImpl>
): Renderer(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { SkinnedMeshRendererImpl(ObjectReader(assetFile, info)) })

    val mMesh get() = container.impl.mMesh
    val mBones get() = container.impl.mBones
    val mBlendShapeWeights get() = container.impl.mBlendShapeWeights
}