package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

class BuildSettingsImpl internal constructor(reader: ObjectReader): ObjectImpl(reader) {
    val mVersion: String

    init {
        reader.readNextStringArray()
        reader += 4     //hasRenderTexture, hasPROVersion, hasPublishingRights, hasShadows: Boolean
        mVersion = reader.readAlignedString()
    }
}