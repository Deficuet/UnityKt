package io.github.deficuet.unitykt.internal

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.ReaderConfig
import io.github.deficuet.unitykt.internal.file.*
import io.github.deficuet.unitykt.internal.file.AbstractFile
import io.github.deficuet.unitykt.internal.file.BundleFile
import io.github.deficuet.unitykt.internal.file.FileNode
import io.github.deficuet.unitykt.internal.file.FileType
import io.github.deficuet.unitykt.internal.file.readerFileType
import io.github.deficuet.unitykt.internal.utils.CompressUtils
import io.github.deficuet.unitykt.utils.EndianBinaryFileReader
import io.github.deficuet.unitykt.utils.EndianByteArrayReader
import io.github.deficuet.unitykt.utils.EndianBinaryReader
import java.io.File
import java.nio.ByteOrder
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
        init(EndianBinaryFileReader(file, config = rc))
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
        init(EndianByteArrayReader(bytes, config = rc))
    }

    private fun init(reader: EndianBinaryReader) {
        when (readerFileType(reader)) {
            FileType.BUNDLE -> BundleFile(reader, this, identifier)
            FileType.WEB -> WebFile(reader, this, identifier)
            FileType.GZIP -> init(
                EndianByteArrayReader(
                    CompressUtils.gzipDecompress(reader.bytes),
                    endian = ByteOrder.LITTLE_ENDIAN
                )
            )
            FileType.BROTLI -> init(
                EndianByteArrayReader(
                    CompressUtils.brotliDecompress(reader.bytes),
                    endian = ByteOrder.LITTLE_ENDIAN
                )
            )
//            FileType.ASSETS -> SerializedFile(reader, this, name).also {
//                manager.assetFiles[name.lowercase()] = it
//            }
            FileType.RESOURCE -> ResourceFile(reader, this, identifier).also {
                manager.resourceFiles[identifier] = it
            }
            else -> {}
        }
    }
}