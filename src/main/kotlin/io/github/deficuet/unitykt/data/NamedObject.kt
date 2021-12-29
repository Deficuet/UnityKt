package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

open class NamedObject protected constructor(reader: ObjectReader): EditorExtension(reader) {
    val mName = reader.readAlignedString()
}