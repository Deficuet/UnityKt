package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.IntRef
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.byteArrayOf
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
        private operator fun <E> List<E>.get(i: IntRef) = this[i.value]

        fun readTypeString(reader: ObjectReader): String {
            reader.position = 0
            val builder = StringBuilder()
            val iRef = IntRef(0)
            while (iRef < nodes.size) {   //for (int i = 0; i < nodes.size; i++)
                readNodeString(builder, nodes, reader, iRef)   // readNodeString(..., ref i)
                iRef += 1
            }
            return builder.toString()
        }

        fun readType(reader: ObjectReader): Map<String, Any> {
            reader.position = 0
            val dict = mutableMapOf<String, Any>()
            val iRef = IntRef(1)
            while (iRef < nodes.size) {
                val node = nodes[iRef]
                dict[node.name] = nodes.readNode(reader, iRef)
                iRef += 1
            }
            return dict
        }

        private fun readNodeString(
            builder: StringBuilder, nodes: List<TreeNode>,
            reader: ObjectReader, intRef: IntRef
        ) {
            val node = nodes[intRef]
            var append = true
            var align = (node.metaFlag and 0x4000) != 0
            var value: Any? = null
            when (node.type) {
                "SInt8" -> value = reader.readSByte()
                "UInt8", "char" -> value = reader.readByte()
                "SInt16", "short" -> value = reader.readShort()
                "UInt16", "unsigned short" -> value = reader.readUShort()
                "SInt32", "int" -> value = reader.readInt()
                "UInt32", "unsigned int", "Type*" -> value = reader.readUInt()
                "SInt64", "long long" -> value = reader.readLong()
                "UInt64", "unsigned long long", "FileSize" -> value = reader.readULong()
                "float" -> value = reader.readFloat()
                "double" -> value = reader.readDouble()
                "bool" -> value = reader.readBool()
                "string" -> {
                    append = false
                    val str = reader.readAlignedString()
                    builder.append("${"\t".repeat(node.level)}${node.type} ${node.name} = \"$str\"\r\n")
                    intRef += 3
                }
                "map" -> {
                    if ((nodes[intRef + 1].metaFlag and 0x4000) != 0) align = true
                    append = false
                    val size = reader.readInt()
                    with(builder) {
                        append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                        append("${"\t".repeat(node.level + 1)}Array Array\r\n")
                        append("${"\t".repeat(node.level + 1)}int size = $size\r\n")
                    }
                    val map = nodes.getNode(intRef.value)
                    intRef += map.size - 1
                    val first = map.getNode(4)
                    val second = map.getNode(first.size + 4)
                    with(builder) {
                        for (i in 0 until size) {
                            append("${"\t".repeat(node.level + 2)}[${i}]\r\n")
                            append("${"\t".repeat(node.level + 2)}pair data\r\n")
                            readNodeString(builder, first, reader, IntRef(0))
                            readNodeString(builder, second, reader, IntRef(0))
                        }
                    }
                }
                "TypelessData" -> {
                    append = false
                    val size = reader.readInt()
                    reader += size
                    intRef += 2
                    with(builder) {
                        append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                        append("${"\t".repeat(node.level)}int size = $size\r\n")
                    }
                }
                else -> {
                    if (intRef < nodes.size - 1 && nodes[intRef + 1].type == "Array") {
                        if ((nodes[intRef + 1].metaFlag and 0x4000) != 0) align = true
                        append = false
                        val size = reader.readInt()
                        with(builder) {
                            append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                            append("${"\t".repeat(node.level + 1)}Array Array\r\n")
                            append("${"\t".repeat(node.level + 1)}int size = $size\r\n")
                        }
                        val vector = nodes.getNode(intRef.value)
                        intRef += vector.size - 1
                        for (j in 0 until size) {
                            builder.append("${"\t".repeat(node.level + 2)}[${j}]\r\n")
                            readNodeString(builder, vector, reader, IntRef(3))
                        }
                    } else {
                        append = false
                        builder.append("${"\t".repeat(node.level)}${node.type} ${node.name}\r\n")
                        val clazz = nodes.getNode(intRef.value)
                        intRef += clazz.size - 1
                        val kRef = IntRef(1)
                        while (kRef < clazz.size) {
                            readNodeString(builder, clazz, reader, kRef)
                            kRef += 1
                        }
                    }
                }
            }
            if (append) builder.append("${"\t".repeat(node.level)}${node.type} ${node.name} = $value\n")
            if (align) reader.alignStream()
        }

        private fun List<TreeNode>.readNode(reader: ObjectReader, iRef: IntRef): Any {
            val node = this[iRef]
            var align = node.metaFlag.and(0x4000) != 0
            val value: Any = when (node.type) {
                "SInt8" -> reader.readSByte()
                "UInt8" -> reader.readByte()
                "char" -> String(reader.read(2))
                "SInt16", "short" -> reader.readShort()
                "UInt16", "unsigned short" -> reader.readUShort()
                "SInt32", "int" -> reader.readInt()
                "UInt32", "unsigned int", "Type*" -> reader.readUInt()
                "SInt64", "long long" -> reader.readLong()
                "UInt64", "unsigned long long", "FileSize" -> reader.readULong()
                "float" -> reader.readFloat()
                "double" -> reader.readDouble()
                "bool" -> reader.readBool()
                "string" -> {
                    iRef += 3
                    reader.readAlignedString()
                }
                "map" -> {
                    if (this[iRef + 1].metaFlag.and(0x4000) != 0) align = true
                    val map = getNode(iRef.value)
                    iRef += map.size - 1
                    val first = map.getNode(4)
                    val second = map.getNode(first.size + 4)
                    val size = reader.readInt()
                    val dict = mutableListOf<Pair<Any, Any>>()
                    for (i in 0 until size) {
                        dict.add(Pair(
                            first.readNode(reader, IntRef(0)),
                            second.readNode(reader, IntRef(0))
                        ))
                    }
                    dict
                }
                "TypelessData" -> {
                    iRef += 2
                    with(reader) { read(readInt()) }
                }
                else -> {
                    if (iRef < size - 1 && this[iRef + 1].type == "Array") {
                        if ((this[iRef + 1].metaFlag and 0x4000) != 0) align = true
                        val vector = getNode(iRef.value)
                        iRef += vector.size - 1
                        val size = reader.readInt()
                        val list = mutableListOf<Any>()
                        for (j in 0 until size) {
                            list.add(vector.readNode(reader, IntRef(3)))
                        }
                        list
                    } else {
                        val clazz = getNode(iRef.value)
                        iRef += clazz.size - 1
                        val dict = mutableMapOf<String, Any>()
                        val kRef = IntRef(1)
                        while (kRef < clazz.size) {
                            val theClass = clazz[kRef]
                            dict[theClass.name] = clazz.readNode(reader, kRef)
                            kRef += 1
                        }
                        dict
                    }
                }
            }
            if (align) reader.alignStream()
            return value
        }

        private fun List<TreeNode>.getNode(index: Int): List<TreeNode> {
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
        constructor(data: SerializedFile.SerializedTypeTreeNodeInternal, type: String, name: String):
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