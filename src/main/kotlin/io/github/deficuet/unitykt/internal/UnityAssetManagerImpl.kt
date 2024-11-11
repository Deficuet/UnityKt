package io.github.deficuet.unitykt.internal

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.ManagerConfig
import io.github.deficuet.unitykt.ReaderConfig
import io.github.deficuet.unitykt.UnityAssetManager
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

internal class UnityAssetManagerImpl(val config: ManagerConfig): UnityAssetManager {
    init {
        UnityAssetManager.managers.add(this)
    }

    internal val serializedFiles = mutableMapOf<String, Any>()  // TODO
    internal val resourceFiles = mutableMapOf<String, Any>()    // TODO
    internal val otherResources = mutableListOf<Closeable>()

    override val contexts = mutableMapOf<String, ImportContextImpl>()

    override val objectList: List<Any>
        get() = contexts.values.flatMap { context ->
            sequence {
                // TODO
            }
        }

    override val objectMap: Map<Long, Any>
        get() = TODO("Not yet implemented")

    override fun loadFromByteArray(
        data: ByteArray,
        name: String,
        readerConfig: ReaderConfigSetter?
    ): ImportContext {
        return ImportContextImpl(name, data, this, getReaderConfig(readerConfig)).also {
            contexts[it.identifier] = it
        }
    }

    override fun loadFile(file: String, readerConfig: ReaderConfigSetter?) = loadFile(File(file), readerConfig)
    override fun loadFile(file: Path, readerConfig: ReaderConfigSetter?): ImportContext {
        if (!file.isRegularFile()) throw IllegalArgumentException("The parameter \"file\" must be a file")
        return ImportContextImpl(file.toFile(), this, getReaderConfig(readerConfig)).also {
            contexts[it.identifier] = it
        }
    }
    override fun loadFile(file: File, readerConfig: ReaderConfigSetter?): ImportContext {
        if (!file.isFile) throw IllegalArgumentException("The parameter \"file\" must be a file")
        return ImportContextImpl(file, this, getReaderConfig(readerConfig)).also {
            contexts[it.identifier] = it
        }
    }
    override fun loadFiles(vararg files: String, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        return loadFiles(*Array(files.size) { File(files[it]) }, readerConfig = readerConfig)
    }
    override fun loadFiles(vararg files: Path, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        if (files.any { !it.isRegularFile() }) throw IllegalArgumentException("All elements must be files")
        val rc = getReaderConfig(readerConfig)
        return Array(files.size) {
            ImportContextImpl(files[it].toFile(), this, rc).also { c ->
                contexts[c.identifier] = c
            }
        }
    }
    override fun loadFiles(vararg files: File, readerConfig: ReaderConfigSetter?): Array< ImportContext> {
        if (files.any { !it.isFile }) throw IllegalArgumentException("All elements must be files")
        val rc = getReaderConfig(readerConfig)
        return Array(files.size) {
            ImportContextImpl(files[it], this, rc).also { c ->
                contexts[c.identifier] = c
            }
        }
    }
    override fun loadFolder(folder: String, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        return loadFolder(Path(folder), readerConfig)
    }
    override fun loadFolder(folder: File, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        return loadFolder(folder.toPath(), readerConfig)
    }
    override fun loadFolder(folder: Path, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        if (!folder.isDirectory()) throw IllegalArgumentException("The parameter \"folder\" must be a directory")
        val files = Files.newDirectoryStream(folder).use { stream ->
            stream.filter { it.isRegularFile() }.toTypedArray()
        }
        return loadFiles(*files, readerConfig = readerConfig)
    }
    override fun loadFolderRecursively(
        folder: String,
        readerConfig: ReaderConfigSetter?
    ): Array<out ImportContext> {
        return loadFolderRecursively(Path(folder), readerConfig)
    }
    override fun loadFolderRecursively(folder: File, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        return loadFolderRecursively(folder.toPath(), readerConfig)
    }
    override fun loadFolderRecursively(folder: Path, readerConfig: ReaderConfigSetter?): Array<out ImportContext> {
        if (!folder.isDirectory()) throw IllegalArgumentException("The parameter \"folder\" must be a directory")
        val files = Files.walk(folder).use {
            it.filter(Files::isRegularFile).collect(Collectors.toList()).toTypedArray()
        }
        return loadFiles(*files, readerConfig = readerConfig)
    }

    override fun close() {
        //TODO
//        serializedFiles.values.forEach { it.reader.close() }
//        resourceFiles.values.forEach { it.reader.close() }
        otherResources.forEach { it.close() }
        serializedFiles.clear()
        resourceFiles.clear()
        otherResources.clear()
        contexts.clear()
    }

    private fun getReaderConfig(setter: ReaderConfigSetter?): ReaderConfig {
        return with(config.readerConfig) {
            setter?.let { copy().apply(it) } ?: this
        }
    }
}