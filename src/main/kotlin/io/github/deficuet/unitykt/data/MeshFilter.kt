package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MeshFilterImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MeshFilter private constructor(
    private val container: ImplementationContainer<MeshFilterImpl>
): Component(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MeshFilterImpl(ObjectReader(assetFile, info)) })

    val mMesh get() = container.impl.mMesh
}