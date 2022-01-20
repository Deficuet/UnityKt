package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

open class Object internal constructor(private val container: ImplementationContainer<ObjectImpl>) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { ObjectImpl(ObjectReader(assetFile, info)) })

    val assetFile = container.assetFile
    val type = container.info.type
    val mPathID = container.info.mPathID
    val unityVersion = container.assetFile.version
    val byteSize = container.info.byteSize
    val platform = assetFile.targetPlatform
    val serializedType = container.info.serializedType
    val bytes get() = container.impl.bytes

    fun dump() = container.impl.dump()
    fun toType() = container.impl.toType()
}