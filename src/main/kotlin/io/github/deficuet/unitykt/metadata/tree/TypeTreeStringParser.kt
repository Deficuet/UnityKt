package io.github.deficuet.unitykt.metadata.tree

internal class TypeTreeStringParser(
    private val lineSeparator: String = "\r\n"
): TypeTreeParser {
    private val builder = StringBuilder()

    override fun writeNode(node: TypeTreeNode) {
        builder.append("${indent(node)}${node.type} ${node.name}").append(lineSeparator)
    }

    override fun writeNodePrimitive(node: TypeTreeNode, value: Any) {
        builder.append("${indent(node)}${node.type} ${node.name} = $value").append(lineSeparator)
    }

    override fun writeString(node: TypeTreeNode, str: String) {
        builder.append("${indent(node)}${node.type} ${node.name} = \"${str}\"").append(lineSeparator)
    }

    override fun writeArrayHeader(node: TypeTreeNode, size: Int) {
        writeNode(node)
        val subNode = node.children[0]
        writeNode(subNode)
        val sizeNode = subNode.children[0]
        writeNodePrimitive(sizeNode, size)
    }

    override fun writeElementIndex(elementNode: TypeTreeNode, index: Int) {
        builder.append("${indent(elementNode)}[$index]").append(lineSeparator)
    }

    override fun flush() = builder.toString()

    private fun indent(node: TypeTreeNode): String {
        return "\t".repeat(node.level)
    }
}
