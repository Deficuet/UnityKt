package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.toJSONObject
import org.json.JSONObject

open class ObjectImpl internal constructor(private val reader: ObjectReader) {
    val assetFile = reader.assetFile
    val mPathID = reader.mPathID
    val unityVersion = reader.unityVersion
    protected val buildType = reader.buildType
    val platform = reader.platform
    private val serializedType = reader.serializedType
    val bytes: ByteArray get() { return reader.bytes }

    init {
        println("Object(${reader.type}) path id $mPathID initialized")
        reader.position = 0
        if (platform == BuildTarget.NoTarget) reader += 4   //m_ObjectHideFlags: UInt
    }

    fun dump() = serializedType?.typeTree?.readTypeString(reader)
    fun toType() = serializedType?.typeTree?.readType(reader)

    val typeTreeJson: JSONObject? get() = toType()?.toJSONObject()
    val typeTreeJsonString: String get() = typeTreeJson?.toString(4) ?: "null"
}