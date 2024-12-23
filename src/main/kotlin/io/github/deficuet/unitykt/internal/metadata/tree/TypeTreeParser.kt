package io.github.deficuet.unitykt.internal.metadata.tree

internal interface TypeTreeParser {
    fun writeNode(node: TypeTreeNodeImpl)
    fun writeNodePrimitive(node: TypeTreeNodeImpl, value: Any)
    fun writeString(node: TypeTreeNodeImpl, str: String)
    fun writeArrayHeader(node: TypeTreeNodeImpl, size: Int)
    fun writeElementIndex(elementNode: TypeTreeNodeImpl, index: Int)
    fun flush(): String
}
