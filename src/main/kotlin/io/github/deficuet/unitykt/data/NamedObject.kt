package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.NamedObjectImpl

abstract class NamedObject internal constructor(
    private val container: ImplementationContainer<NamedObjectImpl>
): EditorExtension(container) {
    val mName: String get() = container.impl.mName
}