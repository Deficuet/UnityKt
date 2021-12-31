package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class AssetBundle internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mPreloadTable = reader.readArrayOf { PPtr<Object>(reader) }
    val mContainer = reader.readArrayOf { reader.readAlignedString() to AssetInfo(reader) }.toMap()
}

class AssetInfo internal constructor(reader: ObjectReader) {
    val preloadIndex = reader.readInt()
    val preloadSize = reader.readInt()
    val asset = PPtr<Object>(reader)
}