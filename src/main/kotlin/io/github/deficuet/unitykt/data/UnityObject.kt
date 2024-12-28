package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.metadata.DataBinding
import io.github.deficuet.unitykt.metadata.UnityObjectCompanion
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata
import io.github.deficuet.unitykt.metadata.tree.TypeTreeParser
import io.github.deficuet.unitykt.metadata.tree.TypeTreeStringParser

open class UnityObject(val metadata: UnityObjectMetadata): DataBinding() {
    val context           get() = metadata.context
    val classType         get() = metadata.classType
    val mPathID           get() = metadata.mPathID
    val unityVersion      get() = metadata.unityVersion
    val buildTarget       get() = metadata.buildTarget
    val serializedType    get() = metadata.serializedType

    fun dump(parser: TypeTreeParser = TypeTreeStringParser()) = metadata.dump(parser)

    companion object: UnityObjectCompanion<UnityObject>(ClassIDType.Object, ::UnityObject)
}
