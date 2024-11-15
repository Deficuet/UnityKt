package io.github.deficuet.unitykt.metadata

interface TypeTreeNode {
    val byteSize: Int
    val index: Int
    val typeFlags: Int
    val version: Int
    val metaFlag: Int
    val level: Int
    val refTypeHash: ULong
    val type: String
    val name: String
}
