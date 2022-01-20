package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

class TextAssetImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mScript = reader.readNextByteArray()
}