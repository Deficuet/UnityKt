package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata

internal class UnityObjectMetadataImpl(
    val serializedFile: SerializedFile,
    val byteStart: Long,
    val byteSize: UInt,
    override val typeID: Int,
    override val classID: Int,
    val isDestroyed: UShort,
    val stripped: UByte,
    override val m_PathID: Long,
    override val serializedType: SerializedTypeImpl?
): UnityObjectMetadata {
    override val classType = ClassIDType.of(classID)

    private var isInitialized = false
    private val valueMap = mutableMapOf<String, Any?>()
    private val dumpBuilder = StringBuilder()

    private fun readTree() {
        val reader = ObjectReader(serializedFile, this)
    }
}
