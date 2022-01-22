package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.*
import io.github.deficuet.unitykt.util.ObjectReader

open class Object internal constructor(private val container: ImplementationContainer<ObjectImpl>) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { ObjectImpl(ObjectReader(assetFile, info)) })

    val assetFile: SerializedFile = container.assetFile
    val type: ClassIDType = container.info.type
    val mPathID: Long = container.info.mPathID
    val unityVersion: IntArray = container.assetFile.version
    val byteSize: UInt = container.info.byteSize
    val platform: BuildTarget = assetFile.targetPlatform
    val serializedType: SerializedType? = container.info.serializedType
    val bytes: ByteArray get() = container.impl.bytes

    /**
     * @see [SerializedType.Tree.readTypeString]
     */
    fun dump() = container.impl.dump()

    /**
     * @see [SerializedType.Tree.readType]
     */
    fun toType() = container.impl.toType()
}