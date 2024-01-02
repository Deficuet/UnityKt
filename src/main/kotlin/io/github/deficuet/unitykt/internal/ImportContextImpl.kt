package io.github.deficuet.unitykt.internal

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.ReaderConfig
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.internal.file.*
import io.github.deficuet.unitykt.util.CompressUtils
import io.github.deficuet.unitykt.util.EndianBinaryFileReader
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayReader
import java.io.File
import java.nio.ByteOrder

internal class ImportContextImpl: ImportContext, FileNode {
    override val bundleParent = this
    override val root = this

    override val name: String
    override val parent: String
    override val manager: UnityAssetManagerImpl
    override val readerConfig: ReaderConfig
    override val objectMap = mutableMapOf<Long, UnityObject>()
    override val objectList: Collection<UnityObject> get() = objectMap.values

    internal constructor(
        file: File,
        manager: UnityAssetManagerImpl,
        config: ReaderConfig
    ) {
        parent = file.parentFile.canonicalPath
        name = file.name
        this.manager = manager
        readerConfig = config
        init(EndianBinaryFileReader(file, config = config))
    }

    internal constructor(
        data: ByteArray,
        name: String,
        manager: UnityAssetManagerImpl,
        config: ReaderConfig
    ) {
        parent = ""
        this.name = name
        this.manager = manager
        readerConfig = config
        init(EndianByteArrayReader(data, config = config))
    }

    private fun init(reader: EndianBinaryReader): AssetFile {
        return when (readerFileType(reader)) {
            FileType.BUNDLE -> BundleFile(reader, this, name)
            FileType.WEB -> WebFile(reader, this, name)
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
            FileType.ASSETS -> SerializedFile(reader, this, name).also {
                manager.assetFiles[name.lowercase()] = it
            }
            FileType.RESOURCE -> ResourceFile(reader, this, name).also {
                manager.resourceFiles[name] = it
            }
        }
    }
}