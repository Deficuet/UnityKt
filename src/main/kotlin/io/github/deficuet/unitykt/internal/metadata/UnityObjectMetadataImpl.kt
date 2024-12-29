package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata
import io.github.deficuet.unitykt.metadata.tree.TypeTreeParser

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

    internal fun readTree(obj: UnityObject) {
        externalLinker.root.manager.config.debugOutput("Object($classType) path id $mPathID initialized")
        serializedType.typeTree.readTree(obj, reader)
    }

    override fun dump(parser: TypeTreeParser): String {
        return serializedType.typeTree.parseTree(reader, parser)
    }

    override fun dumpStructure(parser: TypeTreeParser): String {
        return serializedType.typeTree.parseTreeStructure(parser)
    }
}
