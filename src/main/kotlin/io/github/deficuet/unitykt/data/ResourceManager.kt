package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ResourceManagerImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class ResourceManager private constructor(
    private val container: ImplementationContainer<ResourceManagerImpl>
): Object(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { ResourceManagerImpl(ObjectReader(assetFile, info)) })

    val mContainer get() = container.impl.mContainer
}