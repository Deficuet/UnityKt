package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.file.SerializedType
import io.github.deficuet.unitykt.util.ObjectReader

open class Object internal constructor(private val reader: ObjectReader) {
    val asserFile = reader.assetFile
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
    fun dump(tree: SerializedType.Tree) = tree.readTypeString(reader)
    fun toType() = serializedType?.typeTree?.readType(reader)
    fun toType(tree: SerializedType.Tree) = tree.readType(reader)
}