package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import org.json.JSONObject

internal open class UnityObjectImpl(
    val assetFile: SerializedFile,
    private val info: ObjectInfo
): UnityObject {
    protected val reader = ObjectReader(assetFile, info)

    final override val context            get() = assetFile.root
    final override val type               get() = info.type
    final override val mPathID            get() = info.mPathID
    final override val unityVersion       get() = assetFile.version
    final override val platform           get() = assetFile.targetPlatform
    final override val serializedType     get() = info.serializedType
    final override val bytes              get() = reader.bytes
    val buildType get() = assetFile.buildType

    final override fun dump() = serializedType?.typeTree?.readTypeString(reader)
    final override fun toTypeTree() = serializedType?.typeTree?.readType(reader)

    final override fun toTypeTreeJson(): JSONObject? {
        return toTypeTree()?.let { JSONObject(it) }
    }

    final override fun toTypeTreeJsonString(indent: Int): String {
        return toTypeTreeJson()?.toString(indent) ?: "null"
    }

    protected open fun read() {
        println("Object($type) path id $mPathID initialized")
        reader.position = 0
        if (platform == BuildTarget.NoTarget) reader.skip(4)   //m_ObjectHideFlags: UInt
    }

    private var initialized = false

    protected fun checkInitialize() {
        if (!initialized) {
            read()
            initialized = true
        }
    }
}