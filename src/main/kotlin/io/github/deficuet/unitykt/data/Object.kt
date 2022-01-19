package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

open class Object protected constructor(private val container: ImplementationContainer<ObjectImpl>) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { ObjectImpl(ObjectReader(assetFile, info)) })

    val assetFile get() = container.impl.assetFile
    val type get() = container.impl.type
    val mPathID get() = container.impl.mPathID
    val unityVersion get() = container.impl.unityVersion
    val byteSize get() = container.impl.byteSize
    val platform get() = container.impl.platform
    val serializedType get() = container.impl.serializedType
    val bytes get() = container.impl.bytes

    fun dump() = container.impl.dump()
    fun toType() = container.impl.toType()
}