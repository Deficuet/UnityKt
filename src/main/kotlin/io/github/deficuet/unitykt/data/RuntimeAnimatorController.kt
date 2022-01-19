package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.RuntimeAnimatorControllerImpl

abstract class RuntimeAnimatorController internal constructor(
    container: ImplementationContainer<RuntimeAnimatorControllerImpl>
): NamedObject(container)