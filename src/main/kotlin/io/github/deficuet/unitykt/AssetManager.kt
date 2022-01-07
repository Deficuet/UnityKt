package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object
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
    val objectDict = mutableListOf<Pair<Long, Object>>()
    /**
     * @see [OffsetMode]
     */
    var offsetMode = OffsetMode.AUTO
    var manualIgnoredOffset: Long = 0

    fun loadFromByteArray(data: ByteArray, name: String): ImportContext {
        return ImportContext(data, name, offsetMode, manualIgnoredOffset).also { contexts.add(it) }
    }

    fun loadFile(path: String): ImportContext {
        if (!path.isFile()) throw IllegalStateException("\"path\" must be a file")
        return ImportContext(path, offsetMode, manualIgnoredOffset).also { contexts.add(it) }
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