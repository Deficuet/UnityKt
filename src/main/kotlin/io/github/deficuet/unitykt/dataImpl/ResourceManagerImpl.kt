package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.util.ObjectReader

class ResourceManagerImpl internal constructor(reader: ObjectReader): ObjectImpl(reader) {
    val mContainer = reader.readArrayOf {
        with(reader) { readAlignedString() to PPtr<Object>(reader) }
    }
}