package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

abstract class ComponentImpl internal constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mGameObject = PPtr<GameObjectImpl>(reader)
}