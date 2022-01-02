package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class TextAsset internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mScript = reader.readNextByteArray()
}