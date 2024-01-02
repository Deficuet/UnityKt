package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.TextAsset
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.decodeToString
import java.nio.charset.Charset

internal class TextAssetImpl(
    assetFile: SerializedFile, info: ObjectInfo
): TextAsset, TextAssetFields(assetFile, info) {
    override val mScript: ByteArray get() {
        checkInitialize()
        return fmScript
    }

    override fun read() {
        super.read()
        fmScript = reader.readInt8Array()
    }

    override fun getText(charset: Charset) = mScript.decodeToString(charset)
}