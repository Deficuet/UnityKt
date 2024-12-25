package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.internal.metadata.tree.TypeTreeStringParser
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata
import io.github.deficuet.unitykt.utils.UnityVersion

internal class UnityObjectMetadataImpl(
    private val serializedFile: SerializedFile,
    val byteStart: Long,
    val byteSize: UInt,
    override val typeID: Int,
    override val classID: Int,
    override val mPathID: Long,
    override val serializedType: SerializedTypeImpl
): UnityObjectMetadata {
    override val context = serializedFile.root
    override val unityVersion = serializedFile.unityVersion
    override val buildTarget = serializedFile.buildTarget
    override val classType = ClassIDType.of(classID)

    private var isInitialized = false
    private lateinit var dumpString: String
    private lateinit var valueMap: Map<String, Any>

    private fun readTree() {
        serializedFile.root.manager.config.debugOutput("Object($classType) path id $mPathID initialized")
        val parser = TypeTreeStringParser()
        valueMap = serializedType.typeTree.read(
            ObjectReader(serializedFile, this),
            listOf(parser)
        )
        dumpString = parser.flush()
    }

    override fun dump(): String {
        if (!isInitialized) {
            readTree()
            isInitialized = true
        }
        return dumpString
    }

    override fun getValueMap(): Map<String, Any> {
        if (!isInitialized) {
            readTree()
            isInitialized = true
        }
        return valueMap
    }
}
