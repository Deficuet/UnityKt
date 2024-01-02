package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.util.EndianBinaryReader

internal class WebFile(
    reader: EndianBinaryReader,
    override val bundleParent: FileNode,
    override val name: String
): AssetFile {

    private val directoryInfo = mutableListOf<DirectoryInfoNode>()

    init {
        reader.readNullString()
        val headLength = reader.readInt32()
        while (reader.position < headLength) {
            directoryInfo.add(
                DirectoryInfoNode(
                    offset = reader.readInt32().toLong(),
                    size = reader.readInt32().toLong(),
                    path = reader.readString(reader.readInt32())
                )
            )
        }
        readFiles(reader, directoryInfo)
        reader.close()
    }
}