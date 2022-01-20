package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile

class ImplementationContainer<out T: ObjectImpl> internal constructor(
    internal val assetFile: SerializedFile,
    internal val info: ObjectInfo,
    implConstructor: () -> T
) {
    val impl: T by lazy(implConstructor)
}