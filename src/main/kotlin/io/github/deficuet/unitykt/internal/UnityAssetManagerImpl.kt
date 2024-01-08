package io.github.deficuet.unitykt.internal

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.ReaderConfig
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.internal.file.ResourceFile
import io.github.deficuet.unitykt.internal.file.SerializedFile
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

internal class UnityAssetManagerImpl(
    override val assetRootFolder: Path?,
    override val defaultReaderConfig: ReaderConfig
): UnityAssetManager {
    init { UnityAssetManager.managers.add(this) }

    val assetFiles = mutableMapOf<String, SerializedFile>()
    val resourceFiles = mutableMapOf<String, ResourceFile>()
    override val contexts = mutableMapOf<String, ImportContext>()
    override val objectList get() = contexts.values.flatMap { context ->
        sequence {
            for (obj in context.objectMap) {
                if (obj.key != 1L) {
                    yield(obj.value)
                }
            }
        }
    }
    override val objectMap get() = objectList.groupBy { it.mPathID }

    override fun loadFromByteArray(data: ByteArray, name: String, config: ReaderConfig): ImportContext {
        return ImportContextImpl(data, name, this, config).also {
            contexts[it.name] = it
        }
    }
    override fun loadFile(file: String, config: ReaderConfig) = loadFile(File(file), config)
    override fun loadFile(file: Path, config: ReaderConfig) = loadFile(file.toFile(), config)
    override fun loadFile(file: File, config: ReaderConfig): ImportContext {
        if (!file.isFile) throw IllegalArgumentException("parameter \"file\" must be a file")
        return ImportContextImpl(file, this, config).also {
            contexts[it.name] = it
        }
    }
    override fun loadFiles(vararg files: String, config: ReaderConfig): Array<ImportContext> {
        return loadFiles(*Array(files.size) { File(files[it]) }, config = config)
    }
    override fun loadFiles(vararg files: Path, config: ReaderConfig): Array<ImportContext> {
        return loadFiles(*Array(files.size) { files[it].toFile() }, config = config)
    }
    override fun loadFiles(vararg files: File, config: ReaderConfig): Array<ImportContext> {
        if (files.any { !it.isFile }) throw IllegalArgumentException("all elements in \"files\" must be file")
        return Array(files.size) {
            loadFile(files[it], config)
        }
    }
    override fun loadFolder(folder: String, config: ReaderConfig) = loadFolder(Path(folder), config)
    override fun loadFolder(folder: File, config: ReaderConfig) = loadFolder(folder.toPath(), config)
    override fun loadFolder(folder: Path, config: ReaderConfig): Array<ImportContext> {
        if (!folder.isDirectory()) throw IllegalArgumentException("parameter \"folder\" must be a directory")
        val files = Files.newDirectoryStream(folder).use { stream ->
            stream.filter { it.isRegularFile() }.toTypedArray()
        }
        return loadFiles(*files, config = config)
    }
    override fun loadFolderRecursively(folder: String, config: ReaderConfig): Array<ImportContext> {
        return loadFolderRecursively(Path(folder), config)
    }
    override fun loadFolderRecursively(folder: File, config: ReaderConfig): Array<ImportContext> {
        return loadFolderRecursively(folder.toPath(), config)
    }
    override fun loadFolderRecursively(folder: Path, config: ReaderConfig): Array<ImportContext> {
        if (!folder.isDirectory()) throw IllegalArgumentException("parameter \"path\" must be a directory")
        val files = Files.walk(folder).use {
            it.filter(Files::isRegularFile).collect(Collectors.toList()).toTypedArray()
        }
        return loadFiles(*files, config = config)
    }

    internal val otherReaderList = mutableListOf<Closeable>()

    override fun close() {
        assetFiles.values.forEach { it.reader.close() }
        resourceFiles.values.forEach { it.reader.close() }
        otherReaderList.forEach { it.close() }
        assetFiles.clear()
        resourceFiles.clear()
        otherReaderList.clear()
        contexts.clear()
    }
}