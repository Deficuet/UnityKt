package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.enums.ClassIDType

interface UnityObjectMetadata {
    val typeID: Int
    val classID: Int
    val mPathID: Long
    val serializedType: SerializedType?
    val classType: ClassIDType
}
