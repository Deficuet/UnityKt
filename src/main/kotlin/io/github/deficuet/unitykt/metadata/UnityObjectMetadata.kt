package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.pptr.ExternalLinker
import io.github.deficuet.unitykt.utils.UnityVersion

interface UnityObjectMetadata {
    val context: ImportContext
    val externalLinker: ExternalLinker
    val typeID: Int
    val classID: Int
    val classType: ClassIDType
    val mPathID: Long
    val unityVersion: UnityVersion
    val buildTarget: BuildTarget
    val serializedType: SerializedType

    fun dump(obj: UnityObject): String
}
