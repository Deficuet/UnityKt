package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Texture
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.compareTo

internal abstract class TextureImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Texture, TextureFields(assetFile, info) {
    override fun read() {
        super.read()
        // m_ForcedFallbackFormat: Int, m_DownscaleFallback: Boolean
        // when >= 2020.2: m_IsAlphaChannelOptional: Boolean
        reader.skip(if (unityVersion >= intArrayOf(2020, 2)) 6 else 5)
        reader.alignStream()
    }
}