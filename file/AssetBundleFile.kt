package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.AssetManager
import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.util.*

data class DirectoryInfoNode(
    val path: String,
    val offset: Long,
    val size: Long,
    val flag: UInt = 0u
)

abstract class RawAssetFile {
    abstract val bundleParent: AssetBundleFile
    abstract val name: String
    open val root: ImportContext
        get() = if (bundleParent is ImportContext) bundleParent as ImportContext else bundleParent.root

    companion object {
        val resourceExt = listOf(".resS", ".resource", ".config", ".xml", ".dat")
    }
}

abstract class AssetBundleFile: RawAssetFile() {
    abstract val files: Map<String, RawAssetFile>

    protected fun readFiles(
        reader: EndianBinaryReader, directoryInfo: List<DirectoryInfoNode>
    ): Map<String, RawAssetFile> {
        val fileMap = mutableMapOf<String, RawAssetFile>()
        for (node in directoryInfo) {
            reader.position = node.offset
            val nodeReader = EndianByteArrayReader(
                reader.read(node.size.toInt()),
                baseOffset = reader.baseOffset + node.offset
            )
            val nodeFile = when (nodeReader.fileType) {
                FileType.BUNDLE -> {
                    BundleFile(nodeReader, this, node.path).also {
                        AssetManager.assetBundles[node.path] = it
                    }
                }
                FileType.WEB -> {
                    WebFile(nodeReader, this, node.path).also {
                        AssetManager.assetBundles[node.path] = it
                    }
                }
                FileType.ASSETS -> {
                    if (resourceExt.none { node.path.endsWith(it) }) {
                        SerializedFile(nodeReader, this, node.path).also {
                            AssetManager.assetFiles[node.path] = it
                        }
                    } else {
                        ResourceFile(nodeReader, this, node.path).also {
                            AssetManager.resourceFiles[node.path] = it
                        }
                    }
                }
                FileType.RESOURCE -> ResourceFile(nodeReader, this, node.path).also {
                    AssetManager.resourceFiles[node.path] = it
                }
            }
            fileMap[node.path] = nodeFile
        }
        return fileMap
    }
}