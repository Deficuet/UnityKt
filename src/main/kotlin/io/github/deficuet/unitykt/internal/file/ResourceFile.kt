package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.utils.EndianBinaryReader

internal class ResourceFile(
    internal val reader: EndianBinaryReader,
    override val parent: FileNode,
    override val name: String
): AbstractFile
