package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.EndianBinaryReader

data class ObjectInfo(
    val byteStart: Long,
    val byteSize: UInt,
    val typeID: Int,
    val classID: Int,
    val isDestroyed: UShort,
    val stripped: Byte,
    val mPathID: Long,
    val serializedType: SerializedFile.Type?
)

class ObjectReader(
    private val reader: EndianBinaryReader,
    private val assetFile: SerializedFile,
) {
    private val version = assetFile.header.version
}