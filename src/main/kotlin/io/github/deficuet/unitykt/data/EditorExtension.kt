package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.EditorExtensionImpl

abstract class EditorExtension internal constructor(
    container: ImplementationContainer<EditorExtensionImpl>
): Object(container)