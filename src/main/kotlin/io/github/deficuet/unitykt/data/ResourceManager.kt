package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class ResourceManager internal constructor(reader: ObjectReader): Object(reader) {
    val mContainer = reader.readArrayOf {
        with(reader) { readAlignedString() to PPtr<Object>(reader) }
    }
}