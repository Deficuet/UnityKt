package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.metadata.TypeTreeNode

internal class TypeTreeNodeImpl(
    override val byteSize: Int,
    override val index: Int,
    override val typeFlags: Int,
    override val version: Int,
    override val metaFlag: Int,
    override val level: Int,
    override val refTypeHash: ULong,
    override var type: String = "",
    override var name: String = ""
): TypeTreeNode
