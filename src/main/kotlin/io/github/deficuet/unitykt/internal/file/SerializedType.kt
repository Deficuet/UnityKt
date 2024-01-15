package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.util.ObjectReader
import java.math.BigInteger
import kotlin.collections.set

data class SerializedType(
    val classID: Int = 0,
    val isStrippedType: Boolean = false,
    var scriptTypeIndex: Short = -1,
    val typeTree: Tree = Tree(),
    val scriptID: ByteArray = byteArrayOf(),
    val oldTypeHash: ByteArray = byteArrayOf(),
    val typeDependencies: IntArray = intArrayOf(),
    val className: String = "",
    val nameSpace: String = "",
    val asmName: String = ""
) {
    data class Tree(
        val nodes: MutableList<TreeNode> = mutableListOf()
    ) {
        private data class NodeResult(
            val seq: Int,
            val value: Any
        )

        internal fun readTypeString(reader: ObjectReader): String {
            reader.position = 0
            val builder = StringBuilder()
            var i = 0
            while (i < nodes.size) {
                i = readNodeString(builder, nodes, reader, i)
                i++
            }
            return builder.toString()
        }

        internal fun readType(reader: ObjectReader): Map<String, Any> {
            reader.position = 0
            val dict = mutableMapOf<String, Any>()
            var i = 1
            while (i < nodes.size) {
                val node = nodes[i]
                val result = readNode(reader, nodes, i)
                i = result.seq
                dict[node.name] = result.value
                i++
            }
            return dict
        }

        private fun readNodeString(
            builder: StringBuilder, nodes: List<TreeNode>,
            reader: ObjectReader, i: Int
        ): Int {
            var seq = i
            val node = nodes[seq]
            var append = true
            var align = node.metaFlag.and(0x4000) != 0
            var value: Any? = null
//            println("${node.type} ${node.name}")
            when (node.type) {
                "SInt8" -> value = reader.readInt8()
                "UInt8" -> value = reader.readUInt8()
                "char" -> value = reader.read().toChar()
                "SInt16", "short" -> value = reader.readInt16()
                "UInt16", "unsigned short" -> value = reader.readUInt16()
                "SInt32", "int" -> value = reader.readInt32()
                "UInt32", "unsigned int", "Type*" -> value = reader.readUInt32()
                "SInt64", "long long" -> value = reader.readInt64()
                "UInt64", "unsigned long long", "FileSize" -> value = reader.readUInt64()
                "float" -> value = reader.readFloat()
                "double" -> value = reader.readDouble()
                "bool" -> value = reader.readBool()
                "string" -> {
                    append = false
                    val str = reader.readAlignedString()
                    builder.append("${"\t".repeat(node.level)}${node.type} ${node.name} = \"$str\"\r\n")
                    seq += nodes.getNodes(seq).size - 1
                }
                "map" -> {
                    if (nodes[seq + 1].metaFlag.and(0x4000) != 0) align = true
                    append = false
                    val size = reader.readInt32()
                    with(builder) {
                        append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                        append("${"\t".repeat(node.level + 1)}Array Array\r\n")
                        append("${"\t".repeat(node.level + 1)}int size = $size\r\n")
                    }
                    val map = nodes.getNodes(seq)
                    seq += map.size - 1
                    val first = map.getNodes(4)
                    val second = map.getNodes(first.size + 4)
                    with(builder) {
                        for (j in 0 until size) {
                            append("${"\t".repeat(node.level + 2)}[${j}]\r\n")
                            append("${"\t".repeat(node.level + 2)}pair data\r\n")
                            readNodeString(this, first, reader, 0)
                            readNodeString(this, second, reader, 0)
                        }
                    }
                }
                "TypelessData" -> {
                    append = false
                    val size = reader.readInt32()
                    reader.skip(size)
                    seq += 2
                    with(builder) {
                        append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                        append("${"\t".repeat(node.level)}int size = $size\r\n")
                    }
                }
                else -> {
                    if (seq < nodes.size - 1 && nodes[seq + 1].type == "Array") {
                        if (nodes[seq + 1].metaFlag.and(0x4000) != 0) align = true
                        append = false
                        val size = reader.readInt32()
                        with(builder) {
                            append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                            append("${"\t".repeat(node.level + 1)}Array Array\r\n")
                            append("${"\t".repeat(node.level + 1)}int size = $size\r\n")
                        }
                        val vector = nodes.getNodes(seq)
                        seq += vector.size - 1
                        for (j in 0 until size) {
                            builder.append("${"\t".repeat(node.level + 2)}[${j}]\r\n")
                            readNodeString(builder, vector, reader, 3)
                        }
                    } else {
                        append = false
                        builder.append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                        val clazz = nodes.getNodes(seq)
                        seq += clazz.size - 1
                        var j = 1
                        while (j < clazz.size) {
                            j = readNodeString(builder, clazz, reader, j)
                            j++
                        }
                    }
                }
            }
            if (append) builder.append("${"\t".repeat(node.level)}${node.type} ${node.name} = $value\n")
            if (align) {
//                println("*** alignStream ***")
                reader.alignStream()
            }
            return seq
        }

        private fun readNode(reader: ObjectReader, nodes: List<TreeNode>, i: Int): NodeResult {
            var seq = i
            val node = nodes[seq]
            var align = node.metaFlag.and(0x4000) != 0
            val value: Any
//            println("${node.type} ${node.name}")
            when (node.type) {
                "SInt8" -> value = reader.readInt8()
                "UInt8" -> value = reader.readUInt8().toInt()
                "char" -> value = reader.read().toChar()
                "SInt16", "short" -> value = reader.readInt16()
                "UInt16", "unsigned short" -> value = reader.readUInt16().toInt()
                "SInt32", "int" -> value = reader.readInt32()
                "UInt32", "unsigned int", "Type*" -> value = reader.readUInt32().toLong()
                "SInt64", "long long" -> value = reader.readInt64()
                "UInt64", "unsigned long long", "FileSize" -> {
                    val v = reader.readUInt64()
                    value = BigInteger(1, ByteArray(8) {
                        v.shr((7 - it) * 8).and(0xFFuL).toByte()
                    })
                }
                "float" -> value = reader.readFloat()
                "double" -> value = reader.readDouble()
                "bool" -> value = reader.readBool()
                "string" -> {
                    value = reader.readAlignedString()
                    seq += nodes.getNodes(seq).size - 1
                }
                "map" -> {
                    if (nodes[seq + 1].metaFlag.and(0x4000) != 0) align = true
                    val map = nodes.getNodes(seq)
                    seq += map.size - 1
                    val first = map.getNodes(4)
                    val second = map.getNodes(first.size + 4)
                    val size = reader.readInt32()
                    val dict = mutableListOf<Pair<Any, Any>>()
                    for (j in 0 until size) {
                        dict.add(
                            Pair(
                                readNode(reader, first, 0).value,
                                readNode(reader, second, 0).value
                            )
                        )
                    }
                    value = dict
                }
                "TypelessData" -> {
                    value = reader.readInt8Array()
                    seq += 2
                }
                else -> {
                    if (seq < nodes.size - 1 && nodes[seq + 1].type == "Array") {
                        if (nodes[seq + 1].metaFlag.and(0x4000) != 0) align = true
                        val vector = nodes.getNodes(seq)
                        seq += vector.size - 1
                        val size = reader.readInt32()
                        val list = mutableListOf<Any>()
                        for (j in 0 until size) {
                            list.add(readNode(reader, vector, 3).value)
                        }
                        value = list
                    } else {
                        val clazz = nodes.getNodes(seq)
                        seq += clazz.size - 1
                        val dict = mutableMapOf<String, Any>()
                        var j = 1
                        while (j < clazz.size) {
                            val theClass = clazz[j]
                            val result = readNode(reader, clazz, j)
                            j = result.seq
                            dict[theClass.name] = result.value
                            j++
                        }
                        value = dict
                    }
                }
            }
            if (align) {
//                println("*** alignStream ***")
                reader.alignStream()
            }
            return NodeResult(seq, value)
        }

        private fun List<TreeNode>.getNodes(index: Int): List<TreeNode> {
            val nodes = mutableListOf(this[index])
            val level = this[index].level
            for (i in index + 1 until size) {
                val node = this[i]
                if (node.level <= level) return nodes
                nodes.add(node)
            }
            return nodes
        }
    }

    data class TreeNode(
        val type: String,
        val name: String,
        val byteSize: Int,
        val index: Int,
        val typeFlags: Int,
        val version: Int,
        val metaFlag: Int,
        val level: Int
    ) {
        internal constructor(data: SerializedFile.SerializedTypeTreeNodeInternal, type: String, name: String):
            this(
                type, name, data.byteSize, data.index, data.typeFlags,
                data.version, data.metaFlag, data.level
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedType

        if (classID != other.classID) return false
        if (isStrippedType != other.isStrippedType) return false
        if (scriptTypeIndex != other.scriptTypeIndex) return false
        if (typeTree != other.typeTree) return false
        if (!scriptID.contentEquals(other.scriptID)) return false
        if (!oldTypeHash.contentEquals(other.oldTypeHash)) return false
        if (!typeDependencies.contentEquals(other.typeDependencies)) return false
        if (className != other.className) return false
        if (nameSpace != other.nameSpace) return false
        if (asmName != other.asmName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = classID
        result = 31 * result + isStrippedType.hashCode()
        result = 31 * result + scriptTypeIndex
        result = 31 * result + typeTree.hashCode()
        result = 31 * result + scriptID.contentHashCode()
        result = 31 * result + oldTypeHash.contentHashCode()
        result = 31 * result + typeDependencies.contentHashCode()
        result = 31 * result + className.hashCode()
        result = 31 * result + nameSpace.hashCode()
        result = 31 * result + asmName.hashCode()
        return result
    }
}