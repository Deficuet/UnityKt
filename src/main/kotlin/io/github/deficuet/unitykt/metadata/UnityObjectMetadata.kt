package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.utils.UnityVersion

interface UnityObjectMetadata {
    val context: ImportContext
    val typeID: Int
    val classID: Int
    val classType: ClassIDType
    val m_PathID: Long
    val unityVersion: UnityVersion
    val serializedType: SerializedType

    fun dump(): String
}
