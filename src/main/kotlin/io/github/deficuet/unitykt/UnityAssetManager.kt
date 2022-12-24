package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.file.*
import io.github.deficuet.unitykt.util.OffsetMode
import io.github.deficuet.unitykt.util.isDirectory
import io.github.deficuet.unitykt.util.isFile
import java.io.Closeable
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString

class UnityAssetManager: Closeable {
    /**
     * Usually [BundleFile] and [WebFile] with their file names.
     */
    val assetBundles = mutableMapOf<String, AssetBundleFile>()

    val assetFiles = mutableMapOf<String, SerializedFile>()
    val resourceFiles = mutableMapOf<String, ResourceFile>()

    /**
     * All file context loaded from disk.
     */
    val contexts = mutableListOf<ImportContext>()

    /**
     * All Objects loaded except [io.github.deficuet.unitykt.data.AssetBundle]
     */
    val objects get() = contexts.flatMap { context -> context.objects.filter { it.mPathID != 1L } }

    /**
     * Multi-dictionary of objects associated with their mPathID
     */
    val objectDict get() = objects.map { it.mPathID to it }

    data class Configuration internal constructor(
        /**
         * @see [OffsetMode]
         */
        var offsetMode: OffsetMode = OffsetMode.AUTO,

        /**
         * Skip specific number of bytes before reading under [OffsetMode.MANUAL] mode.
         */
        var manualOffset: Long = 0
    )

    private val configuration = Configuration()

    internal val otherReaderList = mutableListOf<Closeable>()

    init { managers.add(this) }

    /**
     * @param data Bytes of AsserBundle file
     * @param name A string as the "file name" of this bytes
     * @param config To set [Configuration.offsetMode] and [Configuration.manualOffset] **before** reading
     */
    fun loadFromByteArray(data: ByteArray, name: String, config: Configuration.() -> Unit = {}): ImportContext {
        configuration.config()
        return ImportContext(
            data, name, this,
            configuration.offsetMode,
            configuration.manualOffset
        ).also {
            contexts.add(it)
        }
    }

    /**
     * @param path The path string points to a **file**
     * @param config To set [Configuration.offsetMode] and [Configuration.manualOffset] **before** reading
     * @return A [ImportContext] for this file.
     * @throws IllegalStateException If [path] does not point to a **file**
     */
    fun loadFile(path: String, config: Configuration.() -> Unit = {}): ImportContext {
        configuration.config()
        if (!path.isFile()) throw IllegalStateException("\"path\" must be a file")
        return ImportContext(
            path, this,
            configuration.offsetMode,
            configuration.manualOffset
        ).also {
            contexts.add(it)
        }
    }

    /**
     * @param path The path strings point to **files**
     * @param config To set [Configuration.offsetMode] and [Configuration.manualOffset] **before** reading
     * @return A list of [ImportContext] for each file.
     * @throws IllegalStateException If [path] does not point to a **file**
     */
    fun loadFiles(vararg path: String, config: Configuration.() -> Unit = {}): List<ImportContext> {
        configuration.config()
        if (path.any { !it.isFile() }) throw IllegalStateException("\"path\" must be a file")
        return mutableListOf<ImportContext>().apply {
            for (dir in path) {
                add(loadFile(dir))
            }
        }
    }

    /**
     * @param folder The path strings point to a **folder**
     * @param config To set [Configuration.offsetMode] and [Configuration.manualOffset] **before** reading
     * @return A list of [ImportContext] for each file under this folder.
     * Others folders under this directory will be ignored.
     * @throws IllegalStateException If [folder] does not point to a **folder**.
     */
    fun loadFolder(folder: String, config: Configuration.() -> Unit = {}): List<ImportContext> {
        configuration.config()
        if (!folder.isDirectory()) throw IllegalStateException("\"path\" must be a directory")
        val files = Files.newDirectoryStream(Path(folder)).use { stream ->
            stream.filter { it.isRegularFile() }
                .map { it.pathString }
                .toTypedArray()
        }
        return loadFiles(*files)
    }

    /**
     * @param folder The path strings point to a **folder**
     * @param config To set [Configuration.offsetMode] and [Configuration.manualOffset] **before** reading
     * @return A list of [ImportContext] for all reachable files under this folder.
     * @throws IllegalStateException If [folder] does not point to a **folder**.
     */
    fun loadFolderRecursively(folder: String, config: Configuration.() -> Unit = {}): List<ImportContext> {
        configuration.config()
        if (!folder.isDirectory()) throw IllegalStateException("\"path\" must be a directory")
        val files = Files.walk(Path(folder)).filter(Files::isRegularFile)
            .map { it.pathString }.collect(Collectors.toList()).toTypedArray()
        return loadFiles(*files)
    }

    override fun close() {
        assetFiles.values.forEach { it.reader.close() }
        resourceFiles.values.forEach { it.reader.close() }
        otherReaderList.forEach { it.close() }
        assetBundles.clear()
        assetFiles.clear()
        resourceFiles.clear()
        otherReaderList.clear()
        contexts.clear()
    }

    companion object {
        private val managers = mutableListOf<UnityAssetManager>()

        fun closeAll() {
            managers.forEach { it.close() }
        }
    }
}