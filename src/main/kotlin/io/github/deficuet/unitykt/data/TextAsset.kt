package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.TextAssetImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class TextAsset private constructor(
    private val container: ImplementationContainer<TextAssetImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { TextAssetImpl(ObjectReader(assetFile, info)) })

    val mScript get() = container.impl.mScript
}