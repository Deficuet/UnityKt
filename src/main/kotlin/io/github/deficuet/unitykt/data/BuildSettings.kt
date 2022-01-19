package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.BuildSettingsImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class BuildSettings private constructor(
    private val container: ImplementationContainer<BuildSettingsImpl>
): Object(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { BuildSettingsImpl(ObjectReader(assetFile, info)) })

    val mVersion get() = container.impl.mVersion
}