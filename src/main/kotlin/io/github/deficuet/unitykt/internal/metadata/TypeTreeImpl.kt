package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.metadata.TypeTree

internal class TypeTreeImpl(
    override val nodes: MutableList<TypeTreeNodeImpl> = mutableListOf()
): TypeTree {
}