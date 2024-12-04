package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.TypeTree

internal class TypeTreeImpl(
    override val nodes: MutableList<TypeTreeNodeImpl> = mutableListOf()
): TypeTree {
    fun read(
        reader: ObjectReader,
        dumpBuilder: StringBuilder,
        valueMap: MutableMap<String, Any>
    ) {
        reader.position = 0

    }

    private fun readNode(
        reader: ObjectReader,
        nodeList: List<TypeTreeNodeImpl>,

        builder: StringBuilder,
        valueMap: MutableMap<String, Any>,
        i: Int
    ) {
        var seq = i
        val node = nodeList
    }

    private fun getSubNodes(nodeList: List<TypeTreeNodeImpl>, index: Int): List<TypeTreeNodeImpl> {
        val ret = mutableListOf(nodeList[index])
        val level = nodeList[index].level
        for (i in (index + 1) ..< nodeList.size) {
            val node = nodeList[i]
            if (node.level <= level) return ret
            ret.add(node)
        }
        return ret
    }

    fun createNodeTree(): List<TypeTreeNodeImpl> {
        if (nodes.isEmpty()) return emptyList()
        val baseLevel = nodes[0].level
        val rootList = mutableListOf<TypeTreeNodeImpl>()
        val nodeStack = ArrayDeque<TypeTreeNodeImpl>()
        for (node in nodes) {
            if (node.level == baseLevel) {
                rootList.add(node)
                nodeStack.addLast(node)
                continue
            }
            val lastNode = nodeStack.last()
            if (node.level > lastNode.level) {
                lastNode.children.add(node)
            } else {
                var top: TypeTreeNodeImpl
                do {
                    nodeStack.removeLast()
                    top = nodeStack.last()
                } while (node.level <= top.level)
                top.children.add(node)
            }
            nodeStack.addLast(node)
        }
        nodeStack.clear()
        return rootList
    }
}
