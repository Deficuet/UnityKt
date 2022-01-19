package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.RuntimeAnimatorControllerImpl

abstract class RuntimeAnimatorController protected constructor(
    container: ImplementationContainer<RuntimeAnimatorControllerImpl>
): NamedObject(container)