package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MeshRendererImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MeshRenderer private constructor(
    container: ImplementationContainer<MeshRendererImpl>
): Renderer(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { MeshRendererImpl(ObjectReader(assetFile, info)) })
}