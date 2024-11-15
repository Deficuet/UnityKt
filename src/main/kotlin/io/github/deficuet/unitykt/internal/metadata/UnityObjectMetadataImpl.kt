package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata

internal class UnityObjectMetadataImpl(
    val byteStart: Long,
    val byteSize: UInt,
    override val typeID: Int,
    override val classID: Int,
    val isDestroyed: UShort,
    val stripped: UByte,
    override val mPathID: Long,
    override val serializedType: SerializedTypeImpl?
): UnityObjectMetadata {
    override val classType = ClassIDType.of(classID)
    val valueMap = mutableMapOf<String, Any?>()
}
