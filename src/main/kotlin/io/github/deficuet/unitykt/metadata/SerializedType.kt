package io.github.deficuet.unitykt.metadata

interface SerializedType {
    val classID: Int
    val isStrippedType: Boolean
    val scriptTypeIndex: Short
    val typeTree: TypeTree
    val scriptID: ByteArray
    val oldTypeHash: ByteArray
    val typeDependencies: IntArray
    val className: String
    val nameSpace: String
    val asmName: String
}
