package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.MonoScript
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.util.ObjectReader

class MonoBehaviorImpl internal constructor(reader: ObjectReader): BehaviourImpl(reader) {
    val mScript = PPtr<MonoScript>(reader)
    val mName = reader.readAlignedString()
}