package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.EditorExtensionImpl

abstract class EditorExtension protected constructor(
    container: ImplementationContainer<EditorExtensionImpl>
): Object(container)