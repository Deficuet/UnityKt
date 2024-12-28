package io.github.deficuet.unitykt.metadata.tree

interface TypeTreeParser {
    fun writeNode(node: TypeTreeNode)
    fun writeNodePrimitive(node: TypeTreeNode, value: Any)
    fun writeString(node: TypeTreeNode, str: String)
    fun writeArrayHeader(node: TypeTreeNode, size: Int)
    fun writeElementIndex(elementNode: TypeTreeNode, index: Int)
    fun flush(): String
}
