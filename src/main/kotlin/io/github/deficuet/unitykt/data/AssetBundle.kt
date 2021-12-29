package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class AssetBundle internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mPreloadTable = reader.readObjectArrayOf { PPtr<Object>(this) }
    val mContainer = reader.readObjectArrayOf { readAlignedString() to AssetInfo(this) }.toMap()
}

class AssetInfo internal constructor(reader: ObjectReader) {
    val preloadIndex = reader.readInt()
    val preloadSize = reader.readInt()
    val asset = PPtr<Object>(reader)
}