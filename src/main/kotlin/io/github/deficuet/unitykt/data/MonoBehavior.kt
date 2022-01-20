package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MonoBehaviorImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MonoBehavior private constructor(
    private val container: ImplementationContainer<MonoBehaviorImpl>
): Behaviour(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MonoBehaviorImpl(ObjectReader(assetFile, info)) })

    val mScript get() = container.impl.mScript
    val mName get() = container.impl.mName
}