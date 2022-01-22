package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MonoBehaviourImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MonoBehaviour private constructor(
    private val container: ImplementationContainer<MonoBehaviourImpl>
): Behaviour(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MonoBehaviourImpl(ObjectReader(assetFile, info)) })

    val mScript: PPtr<MonoScript>   get() = container.impl.mScript
    val mName: String               get() = container.impl.mName
}