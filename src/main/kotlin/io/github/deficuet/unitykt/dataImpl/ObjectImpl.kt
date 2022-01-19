package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader

open class ObjectImpl internal constructor(private val reader: ObjectReader) {
    val assetFile = reader.assetFile
    val type = reader.type
    val mPathID = reader.mPathID
    val unityVersion = reader.unityVersion
    protected val buildType = reader.buildType
    val byteSize = reader.byteSize
    val platform = reader.platform
    val serializedType = reader.serializedType
    val bytes by lazy { reader.bytes }

    init {
        reader.position = 0
        if (platform == BuildTarget.NoTarget) reader += 4   //m_ObjectHideFlags: UInt
    }

    fun dump() = serializedType?.typeTree?.readTypeString(reader)
    fun toType() = serializedType?.typeTree?.readType(reader)
}