package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.BehaviourImpl

abstract class Behaviour protected constructor(
    private val container: ImplementationContainer<BehaviourImpl>
): Component(container) {
    val mEnabled get() = container.impl.mEnabled
}