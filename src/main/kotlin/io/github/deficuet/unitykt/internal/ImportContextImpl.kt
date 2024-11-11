package io.github.deficuet.unitykt.internal

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.ReaderConfig
import io.github.deficuet.unitykt.internal.file.FileNode
import java.io.File
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

internal class ImportContextImpl: ImportContext, FileNode {
    override val manager: UnityAssetManagerImpl
    override val identifier: String
    override val readerConfig: ReaderConfig

    override val objectMap = mutableMapOf<Long, Any>()
    override val objectList: Collection<Any>
        get() = objectMap.values

    override val parent = this
    override val root = this

    internal constructor(
        file: File,
        m: UnityAssetManagerImpl,
        rc: ReaderConfig
    ) {
        manager = m
        identifier = manager.config.assetSystemRoot
            ?.let { file.toPath().relativeTo(it).pathString }
            ?: file.absolutePath
        readerConfig = rc
    }

    internal constructor(
        name: String,
        bytes: ByteArray,
        m: UnityAssetManagerImpl,
        rc: ReaderConfig
    ) {
        manager = m
        identifier = name
        readerConfig = rc
    }
}