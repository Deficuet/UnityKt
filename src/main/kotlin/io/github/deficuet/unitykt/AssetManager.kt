package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.file.*
import io.github.deficuet.unitykt.util.*
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.*

object AssetManager {
    val assetBundles = mutableMapOf<String, AssetBundleFile>()
    val assetFiles = mutableMapOf<String, SerializedFile>()
    val resourceFiles = mutableMapOf<String, ResourceFile>()
    val contexts = mutableListOf<ImportContext>()
    /**
     * @see [OffsetMode]
     */
    var offsetMode = OffsetMode.AUTO
    private val zipExt = listOf("apk", "zip")

    fun loadFromByteArray(data: ByteArray, name: String): ImportContext {
        return ImportContext(data, name, offsetMode).also { contexts.add(it) }
    }

    fun loadFile(path: String): ImportContext {
        if (!path.isFile()) throw IllegalStateException("\"path\" must be a file")
        return ImportContext(path, offsetMode).also { contexts.add(it) }
    }

    fun loadFiles(vararg path: String): List<ImportContext> {
        if (path.any { !it.isFile() }) throw IllegalStateException("\"path\" must be a file")
        return mutableListOf<ImportContext>().apply {
            for (dir in path) {
                add(loadFile(dir))
            }
        }
    }

    fun loadFolder(folder: String): List<ImportContext> {
        if (!folder.isDirectory()) throw IllegalStateException("\"path\" must be a directory")
        val files = Files.newDirectoryStream(Path(folder)).use { stream ->
            stream.filter { it.isRegularFile() }
                .map { it.pathString }
                .toTypedArray()
        }
        return loadFiles(*files)
    }

    fun loadFolderRecursively(folder: String): List<ImportContext> {
        if (!folder.isDirectory()) throw IllegalStateException("\"path\" must be a directory")
        val files = Files.walk(Path(folder)).filter(Files::isRegularFile)
            .map { it.pathString }.collect(Collectors.toList()).toTypedArray()
        return loadFiles(*files)
    }
}