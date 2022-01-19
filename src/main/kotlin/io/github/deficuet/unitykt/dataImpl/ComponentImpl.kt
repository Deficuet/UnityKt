package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

abstract class ComponentImpl protected constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mGameObject = PPtr<GameObjectImpl>(reader)
}