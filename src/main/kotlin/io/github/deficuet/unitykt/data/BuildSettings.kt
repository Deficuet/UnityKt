package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class BuildSettings internal constructor(reader: ObjectReader): Object(reader) {
    val mVersion: String

    init {
        reader.readNextStringArray()
        reader += 4     //hasRenderTexture, hasPROVersion, hasPublishingRights, hasShadows: Boolean
        mVersion = reader.readAlignedString()
    }
}