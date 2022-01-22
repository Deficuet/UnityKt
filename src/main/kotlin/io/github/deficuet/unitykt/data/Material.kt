package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MaterialImpl
import io.github.deficuet.unitykt.dataImpl.UnityPropertySheet
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Material private constructor(
    private val container: ImplementationContainer<MaterialImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MaterialImpl(ObjectReader(assetFile, info)) })

    val mShader: PPtr<Shader>                   get() = container.impl.mShader
    val mSavedProperties: UnityPropertySheet    get() = container.impl.mSavedProperties
}