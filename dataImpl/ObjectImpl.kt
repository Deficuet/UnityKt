package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader

open class ObjectImpl internal constructor(private val reader: ObjectReader) {
    val assetFile = reader.assetFile
    val mPathID = reader.mPathID
    val unityVersion = reader.unityVersion
    protected val buildType = reader.buildType
    val platform = reader.platform
    private val serializedType = reader.serializedType
    val bytes by lazy { reader.bytes }

    init {
        println("Object with mPathID $mPathID and type ${reader.type} initialized")
        reader.position = 0
        if (platform == BuildTarget.NoTarget) reader += 4   //m_ObjectHideFlags: UInt
    }

    fun dump() = serializedType?.typeTree?.readTypeString(reader)
    fun toType() = serializedType?.typeTree?.readType(reader)
}