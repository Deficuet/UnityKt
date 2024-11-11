package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.internal.ManagerConfigSetter
import io.github.deficuet.unitykt.internal.ReaderConfigSetter
import io.github.deficuet.unitykt.internal.UnityAssetManagerImpl
import java.io.Closeable
import java.io.File
import java.nio.file.Path
import kotlin.io.path.isDirectory

interface UnityAssetManager: Closeable {
    /**
     * All file contexts loaded.
     */
    val contexts: Map<String, ImportContext>

    /**
     * All loaded objects except `AssetBundle`
     * TODO
     */
    val objectList: List<Any>

    /**
     * Multi-dictionary of objects associated with their `mPathID`
     * TODO
     */
    val objectMap: Map<Long, Any>

    /**
     * @param data Byte array data of an asset bundle file
     * @param name A unique string as the name of this bytes
     * @param readerConfig To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading
     * @return A [ImportContext] for the bytes.
     */
    fun loadFromByteArray(data: ByteArray, name: String, readerConfig: ReaderConfigSetter? = null): ImportContext

    /**
     * @param file The **file** path.
     * @param readerConfig To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading
     * @return A [ImportContext] for this file.
     * @throws IllegalArgumentException if [file] does not point to a file.
     */
    fun loadFile(file: String, readerConfig: ReaderConfigSetter? = null): ImportContext
    /**
     * @see loadFile
     */
    fun loadFile(file: Path, readerConfig: ReaderConfigSetter? = null): ImportContext
    /**
     * @see loadFile
     */
    fun loadFile(file: File, readerConfig: ReaderConfigSetter? = null): ImportContext

    /**
     * @param files Arbitrary number of **file** paths.
     * @param readerConfig To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading for **all** files
     * @return An [Array] of [ImportContext] for each file.
     * @throws IllegalArgumentException if any of [files] does not point to a file.
     */
    fun loadFiles(vararg files: String, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>
    /**
     * @see loadFiles
     */
    fun loadFiles(vararg files: Path, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>
    /**
     * @see loadFiles
     */
    fun loadFiles(vararg files: File, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>

    /**
     * @param folder The **folder** path
     * @param readerConfig To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading for **all** files
     * @return An [Array] of [ImportContext] for each file under this folder.
     * Folders under this directory is excluded.
     * @throws IllegalArgumentException if [folder] does not point to a folder.
     */
    fun loadFolder(folder: String, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>
    /**
     * @see loadFolder
     */
    fun loadFolder(folder: File, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>
    /**
     * @see loadFolder
     */
    fun loadFolder(folder: Path, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>

    /**
     * @param folder The **folder** path
     * @param readerConfig To set [offsetMode][ReaderConfig.offsetMode]
     *  and [manualOffset][ReaderConfig.manualOffset] **before** reading for **all** files
     * @return An [Array] of [ImportContext] for **all** reachable files under this folder.
     * @throws IllegalArgumentException if [folder] does not point to a folder.
     */
    fun loadFolderRecursively(folder: String, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>
    /**
     * @see loadFolderRecursively
     */
    fun loadFolderRecursively(folder: File, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>
    /**
     * @see loadFolderRecursively
     */
    fun loadFolderRecursively(folder: Path, readerConfig: ReaderConfigSetter? = null): Array<out ImportContext>

    companion object {
        /**
         * @throws IllegalArgumentException if [assetSystemRoot][ManagerConfig.assetSystemRoot] does not point to a folder.
         */
        fun new(configInit: ManagerConfigSetter = {  }): UnityAssetManager {
            val conf = ManagerConfig().apply(configInit)
            if (conf.assetSystemRoot?.isDirectory() == false) {
                throw IllegalArgumentException("Config assetSystemRoot: ${conf.assetSystemRoot} is not a folder")
            }
            return UnityAssetManagerImpl(conf)
        }

        internal val managers = mutableListOf<UnityAssetManager>()

        fun closeAll() {
            managers.forEach { it.close() }
            managers.clear()
        }
    }
}
