package io.github.deficuet.unitykt.internal.metadata.tree

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.tree.TypeTree

internal class TypeTreeImpl(
    nodeList: List<TypeTreeNodeImpl>
): TypeTree {
    override val nodeTree = createNodeTree(nodeList)

    fun read(reader: ObjectReader, parserList: List<TypeTreeParser>): Map<String, Any> {
        reader.position = 0
        val root = nodeTree[0]
        parserList.forEach { it.writeNode(root) }
        val dict = mutableMapOf<String, Any>()
        for (child in root.children) {
            dict[child.name] = readNode(reader, child, parserList)
        }
        return dict
    }

    private fun readNode(reader: ObjectReader, node: TypeTreeNodeImpl, parserList: List<TypeTreeParser>): Any {
        var align = node.metaFlag.and(0x4000) != 0
        var writeLater = true
        val value: Any
        when (node.dataType) {
            NodeDataType.INT_8 -> value = reader.readInt8()
            NodeDataType.UINT_8 -> value = reader.readUInt8()
            NodeDataType.CHAR -> value = reader.read().toChar()
            NodeDataType.INT_16 -> value = reader.readInt16()
            NodeDataType.UINT_16 -> value = reader.readUInt16()
            NodeDataType.INT_32 -> value = reader.readInt32()
            NodeDataType.UINT_32 -> value = reader.readUInt32()
            NodeDataType.INT_64 -> value = reader.readInt64()
            NodeDataType.UINT_64 -> value = reader.readUInt64()
            NodeDataType.FLOAT -> value = reader.readFloat()
            NodeDataType.DOUBLE -> value = reader.readDouble()
            NodeDataType.BOOL -> value = reader.readBool()
            NodeDataType.STRING -> {
                writeLater = false
                value = reader.readAlignedString()
                parserList.forEach { it.writeString(node, value) }
            }
            NodeDataType.MAP -> {
                writeLater = false
                if (node.children[0].metaFlag.and(0x4000) != 0) {
                    align = true
                }
                val size = reader.readInt32()
                parserList.forEach { it.writeArrayHeader(node, size) }
                val pairNode = node.children[0].children[1]
                val pairs = Array(size) { index ->
                    parserList.forEach {
                        it.writeElementIndex(pairNode, index)
                        it.writeNode(pairNode)
                    }
                    Pair(
                        readNode(reader, pairNode.children[0], parserList),
                        readNode(reader, pairNode.children[1], parserList)
                    )
                }
                value = pairs.groupBy({ it.first }, { it.second })
            }
            NodeDataType.TYPELESS -> {
                writeLater = false
                value = reader.readInt8Array()
                parserList.forEach {
                    it.writeNode(node)
                    it.writeNodePrimitive(
                        TypeTreeNodeImpl(
                            0, 0, 0, 0, 0,
                            node.level + 1, 0uL, "int", "size"
                        ),
                        value.size
                    )
                }
            }
            NodeDataType.MATRIX -> {
                writeLater = false
                value = reader.readMatrix4x4()
                parserList.forEach {
                    it.writeNode(node)
                    for ((i, elementNode) in node.children.withIndex()) {
                        it.writeNodePrimitive(elementNode, value[i])
                    }
                }
            }
            NodeDataType.COMPOSITE -> {
                writeLater = false
                if (node.children.isNotEmpty() && node.children[0].type == "Array") {
                    if (node.children[0].metaFlag.and(0x4000) != 0) {
                        align = true
                    }
                    val size = reader.readInt32()
                    parserList.forEach { it.writeArrayHeader(node, size) }
                    value = readArray(reader, node, size, parserList)
                } else {
                    parserList.forEach { it.writeNode(node) }
                    value = mutableMapOf<String, Any>()
                    for (child in node.children) {
                        value[child.name] = readNode(reader, child, parserList)
                    }
                }
            }
        }
        if (writeLater) {
            parserList.forEach { it.writeNodePrimitive(node, value) }
        }
        if (align) {
            reader.alignStream()
        }
        return value
    }

    private fun readArray(
        reader: ObjectReader,
        arrayNode: TypeTreeNodeImpl,
        size: Int,
        parserList: List<TypeTreeParser>
    ): Any {
        val dataNode = arrayNode.children[0].children[1]
        val value: Any
        if (dataNode.dataType.isPrimitive) {
            val iter: Iterator<Any>
            when (dataNode.dataType) {
                NodeDataType.INT_8 -> {
                    value = reader.readInt8Array(size)
                    iter = value.iterator()
                }
                NodeDataType.UINT_8 -> {
                    value = reader.readUInt8Array(size)
                    iter = value.iterator()
                }
                NodeDataType.INT_16 -> {
                    value = reader.readInt16Array(size)
                    iter = value.iterator()
                }
                NodeDataType.UINT_16 -> {
                    value = reader.readUInt16Array(size)
                    iter = value.iterator()
                }
                NodeDataType.INT_32 -> {
                    value = reader.readInt32Array(size)
                    iter = value.iterator()
                }
                NodeDataType.UINT_32 -> {
                    value = reader.readUInt32Array(size)
                    iter = value.iterator()
                }
                NodeDataType.INT_64 -> {
                    value = reader.readUInt64Array(size)
                    iter = value.iterator()
                }
                NodeDataType.FLOAT -> {
                    value = reader.readFloatArray(size)
                    iter = value.iterator()
                }
                NodeDataType.DOUBLE -> {
                    value = reader.readDoubleArray(size)
                    iter = value.iterator()
                }
                NodeDataType.BOOL -> {
                    value = reader.readBoolArray(size)
                    iter = value.iterator()
                }
                NodeDataType.STRING -> {
                    value = reader.readAlignedStringArray(size)
                    iter = value.iterator()
                }
                else -> throw IllegalStateException("Reading type tree: unknown primitive type")
            }
            parserList.forEach {
                for (i in 0 ..< size) {
                    it.writeElementIndex(dataNode, i)
                    if (dataNode.dataType == NodeDataType.STRING) {
                        it.writeString(dataNode, iter.next().cast())
                    } else {
                        it.writeNodePrimitive(dataNode, iter.next())
                    }
                }
            }
        } else {
            value = Array(size) { index ->
                parserList.forEach {
                    it.writeElementIndex(dataNode, index)
                }
                readNode(reader, dataNode, parserList)
            }
        }
        return value
    }

    companion object {
        internal fun createNodeTree(nodeList: List<TypeTreeNodeImpl>): List<TypeTreeNodeImpl> {
            if (nodeList.isEmpty()) return emptyList()
            val baseLevel = nodeList[0].level
            val rootList = mutableListOf<TypeTreeNodeImpl>()
            val nodeStack = ArrayDeque<TypeTreeNodeImpl>(nodeList.size)
            for (node in nodeList) {
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
}
