package io.github.deficuet.unitykt.internal.metadata.tree

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.metadata.UnityDataClassCompanion
import io.github.deficuet.unitykt.metadata.tree.TypeTree
import kotlin.Any
import kotlin.Array
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.arrayOf
import kotlin.emptyArray
import kotlin.let
import java.lang.reflect.Array as JArray

internal class TypeTreeImpl(
    nodeList: List<TypeTreeNodeImpl>
): TypeTree {
    override val nodeTree = createNodeTree(nodeList)

    fun read(
        obj: UnityObject,
        reader: ObjectReader,
        parserList: List<TypeTreeParser>
    ) {
        reader.position = 0
        val root = nodeTree[0]
        parserList.forEach { it.writeNode(root) }
        for (child in root.children) {
            obj.propertyMap[child.name] = readNode(
                obj, reader, child, parserList,
                obj.loaderMap[child.name] ?: emptyArray()
            )
        }
    }

    private fun readNode(
        obj: UnityObject,
        reader: ObjectReader,
        node: TypeTreeNodeImpl,
        parserList: List<TypeTreeParser>,
        loaders: Array<out UnityDataClassCompanion<*>?>
    ): Any {
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
            NodeDataType.MAP -> {
                writeLater = false
                if (node.children[0].metaFlag.and(0x4000) != 0) {
                    align = true
                }
                val size = reader.readInt32()
                parserList.forEach { it.writeArrayHeader(node, size) }
                val loader1: UnityDataClassCompanion<*>?
                val loader2: UnityDataClassCompanion<*>?
                when (loaders.size) {
                    0 -> { loader1 = null; loader2 = null }
                    1 -> { loader1 = loaders[0]; loader2 = null }
                    else -> { loader1 = loaders[0]; loader2 = loaders[1] }
                }
                val pairNode = node.children[0].children[1]
                val pairs = Array(size) { index ->
                    parserList.forEach {
                        it.writeElementIndex(pairNode, index)
                        it.writeNode(pairNode)
                    }
                    Pair(
                        readNode(
                            obj, reader, pairNode.children[0], parserList,
                            loader1?.let { arrayOf(it) } ?: emptyArray()
                        ),
                        readNode(
                            obj, reader, pairNode.children[1], parserList,
                            loader2?.let { arrayOf(it) } ?: emptyArray()
                        )
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
                        // made up fake node
                        TypeTreeNodeImpl(
                            0, 0, 0, 0, 0,
                            node.level + 1, 0uL, "int", "size"
                        ),
                        value.size
                    )
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
                    value = readArray(obj, reader, node, size, parserList, loaders)
                } else {
                    parserList.forEach { it.writeNode(node) }
                    val loader = if (loaders.isEmpty()) null else loaders[0]
                    val dict: Map<String, Any>
                    val loaderMap: Map<String, Array<out UnityDataClassCompanion<*>?>>
                    if (loader != null) {
                        value = loader.load(obj)
                        dict = value.propertyMap
                        loaderMap = value.loaderMap
                    } else {
                        dict = mutableMapOf()
                        loaderMap = emptyMap()
                        value = dict
                    }
                    for (child in node.children) {
                        dict[child.name] = readNode(
                            obj, reader, child, parserList,
                            loaderMap[child.name] ?: emptyArray()
                        )
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
        obj: UnityObject,
        reader: ObjectReader,
        arrayNode: TypeTreeNodeImpl,
        size: Int,
        parserList: List<TypeTreeParser>,
        loaders: Array<out UnityDataClassCompanion<*>?>
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
            if (loaders.isEmpty() || loaders[0] == null) {
                value = Array(size) { index ->
                    parserList.forEach {
                        it.writeElementIndex(dataNode, index)
                    }
                    readNode(obj, reader, dataNode, parserList, loaders)
                }
            } else {
                value = loaders[0]!!.createArray(size)
                for (i in 0 ..< size) {
                    JArray.set(value, i, readNode(obj, reader, dataNode, parserList, loaders))
                }
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
