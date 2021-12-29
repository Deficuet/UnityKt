package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

abstract class Component protected constructor(reader: ObjectReader): EditorExtension(reader) {
    val mGameObject = PPtr<GameObject>(reader)
}