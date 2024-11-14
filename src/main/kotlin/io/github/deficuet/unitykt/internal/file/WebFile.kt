package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.utils.EndianBinaryReader
import java.nio.ByteOrder

internal class WebFile(
    reader: EndianBinaryReader,
    override val parent: FileNode,
    override val name: String
): AbstractFile {
    private val directoryInfoNodes = mutableListOf<DirectoryInfoNode>()

    init {
        reader.endian = ByteOrder.LITTLE_ENDIAN
        reader.readNullString()
        val headLength = reader.readInt32()
        while (reader.position < headLength) {
            directoryInfoNodes.add(
                DirectoryInfoNode(
                    offset = reader.readInt32().toLong(),
                    size = reader.readInt32().toLong(),
                    path = reader.readString(reader.readInt32())
                )
            )
        }
        readFiles(reader, directoryInfoNodes)
        reader.close()
    }
}
