package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.FontImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Font private constructor(
    private val container: ImplementationContainer<FontImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { FontImpl(ObjectReader(assetFile, info)) })

    val mFontData: ByteArray get() = container.impl.mFontData
}