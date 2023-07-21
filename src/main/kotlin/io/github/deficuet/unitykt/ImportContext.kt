package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.file.*
import io.github.deficuet.unitykt.util.*
import java.io.File

class ImportContext: AssetBundleFile {
    override val files: Map<String, RawAssetFile>
    override val bundleParent = this
    override val root = this

    /**
     * The directory where the file is.
     */
    val directory: String

    /**
     * The name of the file.
     */
    override val name: String

    /**
     * The [UnityAssetManager] which loads this file.
     */
    val manager: UnityAssetManager

    /**
     * All [Object] loaded from this file.
     */
    val objectMap = mutableMapOf<Long, Object>()
    val objectList: Collection<Object> get() = objectMap.values

    internal constructor(
        filePath: String, manager: UnityAssetManager,
        offsetMode: OffsetMode = OffsetMode.MANUAL,
        manualOffset: Long = 0
    ) {
        val file = File(filePath)
        directory = file.parentFile.canonicalPath
        name = file.name
        this.manager = manager
        files = mapOf(name to init(EndianFileStreamReader(
            filePath, offsetMode = offsetMode, manualOffset = manualOffset
        )))
    }

    internal constructor(
        data: ByteArray, name: String, manager: UnityAssetManager,
        offsetMode: OffsetMode = OffsetMode.MANUAL,
        manualOffset: Long = 0
    ) {
        directory = ""
        this.name = name
        this.manager = manager
        files = mapOf(this.name to init(EndianByteArrayReader(
            data, offsetMode = offsetMode, manualOffset = manualOffset
        )))
    }

    private fun init(reader: EndianBinaryReader): RawAssetFile {
        return when (reader.fileType) {
            FileType.BUNDLE -> BundleFile(reader, this, name).also {
                manager.assetBundles[name] = it
            }
            FileType.WEB -> WebFile(reader, this, name).also {
                manager.assetBundles[name] = it
            }
            FileType.ASSETS -> SerializedFile(reader, this, name).also {
                manager.assetFiles[name] = it
            }
            FileType.RESOURCE -> ResourceFile(reader, this, name).also {
                manager.resourceFiles[name] = it
            }
        }
    }
}