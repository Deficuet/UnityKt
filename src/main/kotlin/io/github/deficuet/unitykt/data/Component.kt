package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ComponentImpl

abstract class Component internal constructor(
    private val container: ImplementationContainer<ComponentImpl>
): EditorExtension(container) {
    val mGameObject: PPtr<GameObject> get() = container.impl.mGameObject
}