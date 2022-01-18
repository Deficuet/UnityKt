package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

abstract class TextureImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    init {
        // m_ForcedFallbackFormat: Int, m_DownscaleFallback: Boolean
        // when >= 2020.2: m_IsAlphaChannelOptional: Boolean
        reader += if (unityVersion >= intArrayOf(2020, 2)) 6 else 5
        reader.alignStream()
    }
}