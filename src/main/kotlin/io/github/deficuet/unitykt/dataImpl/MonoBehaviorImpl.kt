package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

class MonoBehaviorImpl internal constructor(reader: ObjectReader): BehaviourImpl(reader) {
    val mScript = PPtr<MonoScriptImpl>(reader)
    val mName = reader.readAlignedString()
}