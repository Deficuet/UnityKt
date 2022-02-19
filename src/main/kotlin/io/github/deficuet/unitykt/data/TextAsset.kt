package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.TextAssetImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.decodeToString
import java.nio.charset.Charset

class TextAsset private constructor(
    private val container: ImplementationContainer<TextAssetImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { TextAssetImpl(ObjectReader(assetFile, info)) })

    val mScript: ByteArray get() = container.impl.mScript

    fun text(charset: Charset = Charsets.UTF_8) = mScript.decodeToString(charset)
}