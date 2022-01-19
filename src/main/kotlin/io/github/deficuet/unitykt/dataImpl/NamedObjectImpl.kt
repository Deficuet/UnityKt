package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

abstract class NamedObjectImpl protected constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mName = reader.readAlignedString()
}