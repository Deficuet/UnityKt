package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.TextureImpl

abstract class Texture internal constructor(
    container: ImplementationContainer<TextureImpl>
): NamedObject(container)