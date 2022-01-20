package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.util.ObjectReader

class AssetBundleImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mPreloadTable = reader.readArrayOf { PPtr<Object>(reader) }
    val mContainer = reader.readArrayOf { reader.readAlignedString() to AssetInfo(reader) }
}

class AssetInfo internal constructor(reader: ObjectReader) {
    val preloadIndex = reader.readInt()
    val preloadSize = reader.readInt()
    val asset = PPtr<Object>(reader)
}