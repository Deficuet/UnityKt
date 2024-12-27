package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.internal.metadata.tree.TypeTreeParser
import io.github.deficuet.unitykt.internal.metadata.tree.TypeTreeStringParser
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata

internal class UnityObjectMetadataImpl(
    override val externalLinker: SerializedFile,
    val byteStart: Long,
    val byteSize: UInt,
    override val typeID: Int,
    override val classID: Int,
    override val mPathID: Long,
    override val serializedType: SerializedTypeImpl
): UnityObjectMetadata {
    override val context = externalLinker.root
    override val unityVersion = externalLinker.unityVersion
    override val buildTarget = externalLinker.buildTarget
    override val classType = ClassIDType.of(classID)

    private val reader = ObjectReader(externalLinker, this)
    private var isParsed = false
    private lateinit var dumpString: String

    private fun readTree(obj: UnityObject) {
        externalLinker.root.manager.config.debugOutput("Object($classType) path id $mPathID initialized")
        val parsers = mutableListOf<TypeTreeParser>()
        if (!isParsed) {
            parsers.add(TypeTreeStringParser())
        }
        serializedType.typeTree.read(obj, reader, parsers)
        if (!isParsed) {
            dumpString = parsers[0].flush()
        }
        obj.isInitialized = true
    }

    override fun dump(obj: UnityObject): String {
        if (!isParsed) {
            readTree(obj)
            isParsed = true
        }
        return dumpString
    }
}
