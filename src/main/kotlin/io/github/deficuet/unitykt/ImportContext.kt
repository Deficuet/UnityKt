package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.classes.UnityObject

interface ImportContext {
    /**
     * File name
     */
    val name: String

    /**
     * Parent directory
     */
    val parent: String

    /**
     * The [UnityAssetManager] which loads this file.
     */
    val manager: UnityAssetManager

    val readerConfig: ReaderConfig

    /**
     * All [UnityObject] loaded from this file.
     */
    val objectMap: Map<Long, UnityObject>
    val objectList: Collection<UnityObject>
}
