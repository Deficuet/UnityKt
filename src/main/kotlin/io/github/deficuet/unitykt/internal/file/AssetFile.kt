package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.internal.ImportContextImpl
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayReader

internal enum class FileType {
    ASSETS, BUNDLE, WEB, RESOURCE, GZIP, BROTLI //, ZIP
}

internal data class DirectoryInfoNode(
    val path: String,
    val offset: Long,
    val size: Long,
    val flag: UInt = 0u
)

internal interface FileNode {
    val bundleParent: FileNode
    val root: ImportContextImpl
        get() = if (bundleParent is ImportContextImpl) bundleParent as ImportContextImpl else bundleParent.root
}

internal interface AssetFile: FileNode {
    val name: String

    fun readFiles(reader: EndianBinaryReader, directoryInfo: List<DirectoryInfoNode>) {
        for (node in directoryInfo) {
            reader.position = node.offset
            val nodeReader = EndianByteArrayReader(
                reader.read(node.size.toInt()),
                baseOffset = reader.baseOffset + node.offset
            )
            when (readerFileType(nodeReader)) {
                FileType.BUNDLE -> BundleFile(nodeReader, this, node.path)
                FileType.WEB -> WebFile(nodeReader, this, node.path)
                FileType.ASSETS -> {
                    if (resourceExt.none { node.path.endsWith(it) }) {
                        SerializedFile(nodeReader, this, node.path).also {
                            root.manager.assetFiles[node.path.lowercase()] = it
                        }
                    } else {
                        ResourceFile(nodeReader, this, node.path).also {
                            root.manager.resourceFiles[node.path] = it
                        }
                    }
                }
                FileType.RESOURCE -> ResourceFile(nodeReader, this, node.path).also {
                    root.manager.resourceFiles[node.path] = it
                }
                else -> {  }
            }
        }
    }

    companion object {
        val resourceExt = arrayOf(".resS", ".resource", ".config", ".xml", ".dat")
    }
}
