package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.classes.AssetBundle
import io.github.deficuet.unitykt.classes.PPtr
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.internal.UnityAssetManagerImpl
import java.io.Closeable
import java.io.File
import java.nio.file.Path

interface UnityAssetManager: Closeable {
    val assetRootFolder: File?
    val defaultReaderConfig: ReaderConfig

    /**
     * All file contexts loaded.
     */
    val contexts: Map<String, ImportContext>

    /**
     * All Objects loaded except [AssetBundle]
     */
    val objectList: List<UnityObject>

    /**
     * Multi-dictionary of objects associated with their mPathID
     */
    val objectMap: Map<Long, List<UnityObject>>

    /**
     * @param data Bytes of AsserBundle file
     * @param name A string as the "file name" of this bytes
     * @param config To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading
     * @return A [ImportContext] for the bytes.
     */
    fun loadFromByteArray(data: ByteArray, name: String, config: ReaderConfig = defaultReaderConfig): ImportContext

    /**
     * @param file The **file** path.
     * @param config To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading
     * @return A [ImportContext] for this file.
     * @throws IllegalArgumentException If [file] does not point to a file.
     */
    fun loadFile(file: String, config: ReaderConfig = defaultReaderConfig): ImportContext
    /**
     * @see loadFile
     */
    fun loadFile(file: Path, config: ReaderConfig = defaultReaderConfig): ImportContext
    /**
     * @see loadFile
     */
    fun loadFile(file: File, config: ReaderConfig = defaultReaderConfig): ImportContext

    /**
     * @param files Arbitrary number of **file** paths.
     * @param config config To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading for all files
     * @return An [Array] of [ImportContext] for each file.
     * @throws IllegalArgumentException If any of [files] does not point to a file.
     */
    fun loadFiles(vararg files: String, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>
    /**
     * @see loadFiles
     */
    fun loadFiles(vararg files: Path, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>
    /**
     * @see loadFiles
     */
    fun loadFiles(vararg files: File, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>

    /**
     * @param folder The **folder** path
     * @param config To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading for all files
     * @return An [Array] of [ImportContext] for each file under this folder.
     * Folders under this directory is excluded.
     * @throws IllegalArgumentException If [folder] does not point to a folder.
     */
    fun loadFolder(folder: String, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>
    /**
     * @see loadFolder
     */
    fun loadFolder(folder: File, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>
    /**
     * @see loadFolder
     */
    fun loadFolder(folder: Path, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>

    /**
     * @param folder The **folder** path
     * @param config To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading for all files
     * @return An [Array] of [ImportContext] for **all** reachable files under this folder.
     * @throws IllegalArgumentException If [folder] does not point to a folder.
     */
    fun loadFolderRecursively(folder: String, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>
    /**
     * @see loadFolderRecursively
     */
    fun loadFolderRecursively(folder: File, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>
    /**
     * @see loadFolderRecursively
     */
    fun loadFolderRecursively(folder: Path, config: ReaderConfig = defaultReaderConfig): Array<ImportContext>

    companion object {
        /**
         * @param assetRootFolder The root folder of a Unity asset bundles system.
         *  This will be used by [PPtr] to find dependency objects.
         */
        fun new(assetRootFolder: String, readerConfig: ReaderConfig = ReaderConfig.default): UnityAssetManager {
            return new(File(assetRootFolder), readerConfig)
        }
        /**
         * @see new
         */
        fun new(assetRootFolder: Path, readerConfig: ReaderConfig = ReaderConfig.default): UnityAssetManager {
            return new(assetRootFolder.toFile(), readerConfig)
        }
        /**
         * @see new
         */
        fun new(assetRootFolder: File, readerConfig: ReaderConfig = ReaderConfig.default): UnityAssetManager {
            if (!assetRootFolder.isDirectory) {
                throw IllegalArgumentException("${assetRootFolder.path} is not a valid folder")
            }
            return UnityAssetManagerImpl(assetRootFolder, readerConfig)
        }

        fun new(readerConfig: ReaderConfig = ReaderConfig.default): UnityAssetManager {
            return UnityAssetManagerImpl(null, readerConfig)
        }

        internal val managers = mutableListOf<UnityAssetManager>()

        fun closeAll() {
            managers.forEach { it.close() }
            managers.clear()
        }
    }
}

data class ReaderConfig internal constructor(
    /**
     * @see [OffsetMode]
     */
    val offsetMode: OffsetMode = OffsetMode.MANUAL,

    /**
     * Skip specific number of bytes before reading.
     *
     * Works under [OffsetMode.MANUAL] mode.
     */
    val manualOffset: Long = 0
) {
    companion object {
        val default = ReaderConfig()
    }
}

enum class OffsetMode {
    MANUAL,

    /**
     * Stream will seek automatically to the first non-zero byte.
     */
    AUTO
}