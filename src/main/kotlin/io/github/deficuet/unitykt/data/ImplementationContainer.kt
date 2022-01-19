package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ObjectImpl

class ImplementationContainer<out T: ObjectImpl> internal constructor(implConstructor: () -> T) {
    val impl: T by lazy(implConstructor)
}