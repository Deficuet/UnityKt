package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class MonoBehavior internal constructor(reader: ObjectReader): Behaviour(reader) {
    val mScript = PPtr<MonoScript>(reader)
    val mName = reader.readAlignedString()
}