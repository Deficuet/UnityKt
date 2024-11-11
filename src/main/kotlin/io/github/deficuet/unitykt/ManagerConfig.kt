package io.github.deficuet.unitykt

import java.nio.file.Path

/**
 * The configurations are set with the lambda parameter when creating a new instance of [UnityAssetManager].
 *
 * @see [UnityAssetManager.new]
 */
data class ManagerConfig internal constructor(
    /**
     * The root folder of a Unity asset bundle file system.
     * This will be used by [PPtr] to find dependency objects.
     * TODO
     */
    var assetSystemRoot: Path? = null,

    /**
     * A map which
     * - The key is the [m_AssetBundleName]() property of an asset bundle file
     * - The value is a list containing the [m_AssetBundleName]() properties of its dependency asset bundle files.
     *
     * Usually, the names are the relative paths related to the [assetSystemRoot].
     *
     * This will be used by [PPtr] to find dependency objects.
     * TODO
     */
    var dependenciesTable: Map<String, List<String>> = emptyMap(),

    /**
     * A function that takes a debug info string and returns void.
     *
     * e.g. if the debug info is intended to be printed to std out, use `::println`.
     */
    var debugOutput: (String) -> Unit = {  },

    /**
     * @see ReaderConfig
     */
    var readerConfig: ReaderConfig = ReaderConfig.default,
)
