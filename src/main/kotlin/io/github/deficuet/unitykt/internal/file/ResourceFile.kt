package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.util.EndianBinaryReader

internal class ResourceFile(
    internal val reader: EndianBinaryReader,
    override val bundleParent: FileNode,
    override val name: String
): AssetFile