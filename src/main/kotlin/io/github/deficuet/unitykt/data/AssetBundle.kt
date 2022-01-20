package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AssetBundleImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AssetBundle private constructor(
    private val container: ImplementationContainer<AssetBundleImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AssetBundleImpl(ObjectReader(assetFile, info)) })

    val mPreloadTable get() = container.impl.mPreloadTable
    val mContainer get() = container.impl.mContainer
}