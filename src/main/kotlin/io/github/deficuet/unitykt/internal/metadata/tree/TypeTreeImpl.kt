package io.github.deficuet.unitykt.internal.metadata.tree

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.internal.utils.ObjectReader
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.metadata.UnityDataClassCompanion
import io.github.deficuet.unitykt.metadata.tree.TypeTree
import io.github.deficuet.unitykt.metadata.tree.TypeTreeParser
import java.lang.reflect.Array as JArray

internal typealias Loader = UnityDataClassCompanion<*>?

internal class TypeTreeImpl(
    nodeList: List<TypeTreeNodeImpl>
): TypeTree {
    override val nodeTree = createNodeTree(nodeList)

    fun readTree(obj: UnityObject, reader: ObjectReader) {
        reader.position = 0
        val root = nodeTree[0]
        for (child in root.children) {
            obj.propertyMap[child.name] = readNodeValue(
                obj, reader, child, obj.loaderMap[child.name] ?: ArrayDeque(0)
            )
        }
    }

    private fun readNodeValue(
        obj: UnityObject,
        reader: ObjectReader,
        node: TypeTreeNodeImpl,
        loaders: ArrayDeque<Loader>
    ): Any {
        var align = node.metaFlag.and(0x4000) != 0
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
            NodeDataType.STRING -> value = reader.readAlignedString()
            NodeDataType.MATRIX -> value = reader.readMatrix4x4()
            NodeDataType.TYPELESS -> value = reader.readInt8Array()
            NodeDataType.MAP -> {
                if (node.children[0].metaFlag.and(0x4000) != 0) {
                    align = true
                }
                val size = reader.readInt32()
                val secondaryLoaders = ArrayDeque(loaders).apply { removeFirstOrNull() }
                val pairNode = node.children[0].children[1]
                val pairs = Array(size) { _ ->
                    Pair(
                        readNodeValue(obj, reader, pairNode.children[0], loaders),
                        readNodeValue(obj, reader, pairNode.children[1], secondaryLoaders)
                    )
                }
                value = pairs.groupBy({ it.first }, { it.second })
            }
            NodeDataType.COMPOSITE -> {
                if (node.children.firstOrNull()?.type == "Array") {
                    if (node.children[0].metaFlag.and(0x4000) != 0) {
                        align = true
                    }
                    val size = reader.readInt32()
                    value = readArray(obj, reader, node, size, loaders)
                } else {
                    val loader = loaders.firstOrNull()
                    val dict: Map<String, Any>
                    val loaderMap: Map<String, ArrayDeque<Loader>>
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
                        dict[child.name] = readNodeValue(
                            obj, reader, child,
                            loaderMap[child.name] ?: ArrayDeque(0)
                        )
                    }
                }
            }
        }
        if (align) reader.alignStream()
        return value
    }

    private fun readArray(
        obj: UnityObject,
        reader: ObjectReader,
        arrayNode: TypeTreeNodeImpl,
        size: Int,
        loaders: ArrayDeque<Loader>
    ): Any {
        val dataNode = arrayNode.children[0].children[1]
        val value: Any
        if (dataNode.dataType.isPrimitive) {
            value = when (dataNode.dataType) {
                NodeDataType.INT_8 -> reader.readInt8Array(size)
                NodeDataType.UINT_8 -> reader.readUInt8Array(size)
                NodeDataType.INT_16 -> reader.readInt16Array(size)
                NodeDataType.UINT_16 -> reader.readUInt16Array(size)
                NodeDataType.INT_32 -> reader.readInt32Array(size)
                NodeDataType.UINT_32 -> reader.readUInt32Array(size)
                NodeDataType.INT_64 -> reader.readUInt64Array(size)
                NodeDataType.FLOAT -> reader.readFloatArray(size)
                NodeDataType.DOUBLE -> reader.readDoubleArray(size)
                NodeDataType.BOOL -> reader.readBoolArray(size)
                NodeDataType.STRING -> reader.readAlignedStringArray(size)
                NodeDataType.MATRIX -> reader.readMatrix4x4Array(size)
                else -> throw IllegalStateException("Reading type tree: unknown primitive type")
            }
        } else {
            when (size) {
                0 -> value = JArray.newInstance(elementClass(dataNode, loaders), 0)
                else -> {
                    val sample = readNodeValue(obj, reader, dataNode, loaders)
                    value = JArray.newInstance(sample::class.java, size).apply {
                        JArray.set(this, 0, sample)
                    }
                    for (i in 1 ..< size) {
                        JArray.set(value, i, readNodeValue(obj, reader, dataNode, loaders))
                    }
                }
            }
        }
        return value
    }

    private fun elementClass(dataNode: TypeTreeNodeImpl, loaders: ArrayDeque<Loader>): Class<*> {
        return when (dataNode.dataType) {
            NodeDataType.INT_8 -> Byte::class.java
            NodeDataType.UINT_8 -> UByte::class.java
            NodeDataType.CHAR -> java.lang.Character::class.java
            NodeDataType.INT_16 -> Short::class.java
            NodeDataType.UINT_16 -> Short::class.java
            NodeDataType.INT_32 -> Int::class.java
            NodeDataType.UINT_32 -> UInt::class.java
            NodeDataType.INT_64 -> Long::class.java
            NodeDataType.UINT_64 -> ULong::class.java
            NodeDataType.FLOAT -> Float::class.java
            NodeDataType.DOUBLE -> Double::class.java
            NodeDataType.BOOL -> Boolean::class.java
            NodeDataType.STRING -> String::class.java
            NodeDataType.MATRIX -> Matrix4x4::class.java
            NodeDataType.TYPELESS -> ByteArray::class.java
            NodeDataType.MAP -> Map::class.java
            NodeDataType.COMPOSITE -> {
                if (dataNode.children.firstOrNull()?.type == "Array") {
                    elementClass(dataNode.children[0].children[1], loaders).arrayType()
                } else {
                    loaders.firstOrNull()?.clazz ?: Any::class.java
                }
            }
        }
    }

    fun parseTree(reader: ObjectReader, parser: TypeTreeParser): String {
        reader.position = 0
        val root = nodeTree[0]
        parser.writeNode(root)
        for (child in root.children) {
            parseNodeValue(reader, parser, child)
        }
        return parser.flush()
    }

    private fun parseNodeValue(reader: ObjectReader, parser: TypeTreeParser, node: TypeTreeNodeImpl) {
        var align = node.metaFlag.and(0x4000) != 0
        var writeLater = true
        var value: Any? = null
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
                parser.writeString(node, reader.readAlignedString())
            }
            NodeDataType.MAP -> {
                writeLater = false
                if (node.children[0].metaFlag.and(0x4000) != 0) {
                    align = true
                }
                val size = reader.readInt32()
                parser.writeArrayHeader(node, size)
                val pairNode = node.children[0].children[1]
                for (i in 0 ..< size) {
                    parser.writeElementIndex(pairNode, i)
                    parser.writeNode(pairNode)
                    parseNodeValue(reader, parser, pairNode.children[0])
                    parseNodeValue(reader, parser, pairNode.children[1])
                }
            }
            NodeDataType.TYPELESS -> {
                writeLater = false
                val size = reader.readInt32()
                reader.skip(size)
                parser.writeNode(node)
                parser.writeNodePrimitive(
                    TypeTreeNodeImpl(
                        0, 0, 0, 0, 0,
                        node.level + 1, 0uL, "int", "size"
                    ),
                    size
                )
            }
            else -> {
                writeLater = false
                if (node.children.firstOrNull()?.type == "Array") {
                    if (node.children[0].metaFlag.and(0x4000) != 0) {
                        align = true
                    }
                    val size = reader.readInt32()
                    parser.writeArrayHeader(node, size)
                    val dataNode = node.children[0].children[1]
                    for (i in 0 ..< size) {
                        parser.writeElementIndex(dataNode, i)
                        parseNodeValue(reader, parser, dataNode)
                    }
                } else {
                    parser.writeNode(node)
                    for (child in node.children) {
                        parseNodeValue(reader, parser, child)
                    }
                }
            }
        }
        if (writeLater) parser.writeNodePrimitive(node, value!!)
        if (align) reader.alignStream()
    }

    fun parseTreeStructure(parser: TypeTreeParser): String {
        for (node in nodeTree) {
            parseNodeMetadata(parser, node)
        }
        return parser.flush()
    }

    private fun parseNodeMetadata(parser: TypeTreeParser, node: TypeTreeNodeImpl) {
        parser.writeNodeMetadata(node)
        for (child in node.children) {
            parseNodeMetadata(parser, child)
        }
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
