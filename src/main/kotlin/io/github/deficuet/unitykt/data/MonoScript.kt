package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MonoScriptImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MonoScript private constructor(
    private val container: ImplementationContainer<MonoScriptImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MonoScriptImpl(ObjectReader(assetFile, info)) })

    val mClassName: String      get() = container.impl.mClassName
    val mNameSpace: String      get() = container.impl.mNameSpace
    val mAssemblyName: String   get() = container.impl.mAssemblyName
}