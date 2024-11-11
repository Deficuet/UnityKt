package io.github.deficuet.unitykt

interface ImportContext {
    /**
     * The [UnityAssetManager] which loads this file
     */
    val manager: UnityAssetManager

    /**
     * For a passed in file, its relative path related to [assetSystemRoot][ManagerConfig.assetSystemRoot]
     *  is used if [assetSystemRoot][ManagerConfig.assetSystemRoot] is not null,
     *  otherwise its absolute path is used.
     *
     * For a byte array, the given unique name is always used.
     */
    val identifier: String

    /**
     * @see ReaderConfig
     */
    val readerConfig: ReaderConfig

    /**
     * All objects loaded from this file. The objects are associated by their `m_PathId`
     * TODO
     */
    val objectMap: Map<Long, Any>
    val objectList: Collection<Any>
}