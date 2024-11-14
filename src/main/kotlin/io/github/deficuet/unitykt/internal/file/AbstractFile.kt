package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.utils.EndianBinaryReader
import io.github.deficuet.unitykt.utils.EndianByteArrayReader

internal enum class FileType {
    ASSETS, BUNDLE, WEB, RESOURCE, GZIP, BROTLI //, ZIP
}

internal data class DirectoryInfoNode(
    val offset: Long,
    val size: Long,
    val path: String,
    val flag: UInt = 0u
)

internal interface AbstractFile: FileNode {
    val name: String

    fun readFiles(reader: EndianBinaryReader, nodes: List<DirectoryInfoNode>) {
        for (node in nodes) {
            reader.position = node.offset
            EndianByteArrayReader(
                reader.read(node.size.toInt()),
                baseOffset = reader.baseOffset + node.offset
            ).use { nodeReader ->
                when (readerFileType(nodeReader)) {
                    FileType.BUNDLE -> BundleFile(nodeReader, this, node.path)
                    FileType.WEB -> WebFile(nodeReader, this, node.path)

                    FileType.RESOURCE -> ResourceFile(nodeReader, this, node.path).also {
                        root.manager.resourceFiles[node.path] = it
                    }
                    else -> {  }
                }
            }
        }
    }

    companion object {
        val RESOURCE_EXT = arrayOf(".resS", ".resource", ".config", ".xml", ".dat")
    }
}
