package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.enums.ClassIDType

interface UnityObjectMetadata {
    val typeID: Int
    val classID: Int
    val m_PathID: Long
    val serializedType: SerializedType
    val classType: ClassIDType

    fun dump(): String
}
