package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.MonoScript
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.util.ObjectReader
import org.json.JSONObject

class MonoBehaviourImpl internal constructor(reader: ObjectReader): BehaviourImpl(reader) {
    val mScript = PPtr<MonoScript>(reader)
    val mName = reader.readAlignedString()

    val json by lazy { toType()?.let { JSONObject(it) } }

    val jsonString by lazy { json?.toString(4) ?: "null" }
}