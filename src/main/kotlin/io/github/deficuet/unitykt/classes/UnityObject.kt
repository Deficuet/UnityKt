package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.file.SerializedType
import org.json.JSONObject

interface UnityObject {
    //region metadata
    val context: ImportContext
    val type: ClassIDType
    val mPathID: Long
    val unityVersion: IntArray
    val platform: BuildTarget
    val serializedType: SerializedType?

    /**
     * Mostly be used to create [PPtr] from the type tree of a [MonoBehaviour]
     */
    fun <T: UnityObject> createPPtr(fileId: Int, pathId: Long): PPtr<T>
    //endregion

    val bytes: ByteArray

    /**
     * @see [SerializedType.Tree.readTypeString]
     */
    fun dump(): String?

    /**
     * @see [SerializedType.Tree.readType]
     */
    fun toTypeTree(): Map<String, Any>?
    fun toTypeTreeJson(): JSONObject?
    fun toTypeTreeJsonString(indent: Int = 4): String
}