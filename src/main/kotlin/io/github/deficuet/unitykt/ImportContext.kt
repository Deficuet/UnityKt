package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.file.*
import io.github.deficuet.unitykt.util.*
import java.io.File

class ImportContext: AssetBundleFile {
    override val files: Map<String, RawAssetFile>
    override val bundleParent = this
    override val root = this
    val directory: String
    override val name: String

    internal constructor(filePath: String, offsetMode: OffsetMode) {
        val file = File(filePath)
        directory = file.parentFile.canonicalPath
        name = file.name
        files = mapOf(name to init(EndianFileStreamReader(filePath, offsetMode = offsetMode)))
    }

    internal constructor(data: ByteArray, name: String, offsetMode: OffsetMode) {
        directory = ""
        this.name = name
        files = mapOf(this.name to init(EndianByteArrayReader(data, offsetMode = offsetMode)))
    }

    private fun init(reader: EndianBinaryReader): RawAssetFile {
        return when (reader.fileType) {
            FileType.BUNDLE -> BundleFile(reader, this, name).also {
                AssetManager.assetBundles[name] = it
            }
            FileType.WEB -> WebFile(reader, this, name).also {
                AssetManager.assetBundles[name] = it
            }
            FileType.ASSETS -> SerializedFile(reader, this, name).also {
                AssetManager.assetFiles[name] = it
            }
            FileType.RESOURCE -> ResourceFile(reader, this, name).also {
                AssetManager.resourceFiles[name] = it
            }
        }
    }
}

operator fun List<ImportContext>.get(key: String) = find { it.name.contentEquals(key) }