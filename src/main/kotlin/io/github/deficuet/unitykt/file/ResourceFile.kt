package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.EndianBinaryReader

class ResourceFile(
    val reader: EndianBinaryReader,
    override val bundleParent: AssetBundleFile,
    override val name: String
): RawAssetFile() {
    fun read(offset: Long, size: Int): ByteArray {
        return with(reader) { position = offset; read(size) }
    }
}