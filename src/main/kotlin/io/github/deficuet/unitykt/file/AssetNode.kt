package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*
import io.github.deficuet.unitykt.util.FileType
import io.github.deficuet.unitykt.util.ImportUtils

data class DirectoryInfoNode(
    val path: String,
    val offset: Long,
    val size: Long,
    val flag: UInt = 0u
)

abstract class AssetNode: AssetNodeOrReader {
    abstract val files: Map<String, Any>

    protected fun readFiles(
        reader: EndianBinaryReader, directoryInfo: List<DirectoryInfoNode>
    ): Map<String, AssetNodeOrReader> {
        val fileMap = mutableMapOf<String, AssetNodeOrReader>()
        for (node in directoryInfo) {
            reader.position = node.offset
            val nodeReader = EndianByteArrayReader(
                reader.read(node.size.toInt()),
                baseOffset = reader.baseOffset + node.offset
            )
            val (nodeType, _) = ImportUtils.checkFileType(nodeReader, OffsetMode.MANUAL)
            var nodeFile: AssetNode? = null
            when (nodeType) {
                FileType.BUNDLE -> nodeFile = BundleFile(nodeReader)
                FileType.WEB -> nodeFile = WebFile(nodeReader)
                FileType.ASSETS -> {
                    if (resourceExt.none { node.path.endsWith(it) }) {
                        nodeFile = SerializedFile(nodeReader)
                    }
                }
                else -> {  }
            }
            fileMap[node.path] = nodeFile ?: nodeReader
        }
        return fileMap
    }

    companion object {
        private val resourceExt = listOf(".resS", ".resource", ".config", ".xml", ".dat")
    }
}