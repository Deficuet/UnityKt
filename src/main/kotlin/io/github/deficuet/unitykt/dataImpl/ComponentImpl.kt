package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.GameObject
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.util.ObjectReader

abstract class ComponentImpl internal constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mGameObject = PPtr<GameObject>(reader)
}