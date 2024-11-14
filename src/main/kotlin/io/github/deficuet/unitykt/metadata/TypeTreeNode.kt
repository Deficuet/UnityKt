package io.github.deficuet.unitykt.metadata

internal class TypeTreeNode(
    val byteSize: Int,
    val index: Int,
    val typeFlags: Int,
    val version: Int,
    val metaFlag: Int,
    val level: Int,
    val typeStrOffset: UInt,
    val nameStrOffset: UInt,
    val refTypeHash: ULong,
    var type: String = "",
    var name: String = ""
)
