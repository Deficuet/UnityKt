package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ComponentImpl

abstract class Component protected constructor(
    private val container: ImplementationContainer<ComponentImpl>
): EditorExtension(container) {
    val mGameObject get() = container.impl.mGameObject
}