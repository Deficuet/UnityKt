package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MonoScriptImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MonoScript private constructor(
    private val container: ImplementationContainer<MonoScriptImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { MonoScriptImpl(ObjectReader(assetFile, info)) })

    val mClassName get() = container.impl.mClassName
    val mNameSpace get() = container.impl.mNameSpace
    val mAssemblyName get() = container.impl.mAssemblyName
}