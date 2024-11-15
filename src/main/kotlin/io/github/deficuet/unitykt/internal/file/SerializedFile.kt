package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.utils.BuildType
import io.github.deficuet.unitykt.utils.UnityVersion
import io.github.deficuet.unitykt.internal.metadata.SerializedTypeImpl
import io.github.deficuet.unitykt.internal.metadata.TypeTreeImpl
import io.github.deficuet.unitykt.internal.metadata.TypeTreeNodeImpl
import io.github.deficuet.unitykt.internal.metadata.UnityObjectMetadataImpl
import io.github.deficuet.unitykt.utils.EndianBinaryReader
import io.github.deficuet.unitykt.utils.EndianByteArrayReader
import io.github.deficuet.unitykt.utils.readArrayOf
import java.io.File
import java.nio.ByteOrder

internal class FormatVersion private constructor() {
    companion object {
//        const val Unsupported = 1u
        const val UNKNOWN_2 = 2u
        const val UNKNOWN_3 = 3u
        const val UNKNOWN_5 = 5u
        const val UNKNOWN_6 = 6u
        const val UNKNOWN_7 = 7u
        const val UNKNOWN_8 = 8u
        const val UNKNOWN_9 = 9u
        const val UNKNOWN_10 = 10u
        const val HAS_SCRIPT_TYPE_INDEX = 11u
        const val UNKNOWN_12 = 12u
        const val HAS_TYPE_TREE_HASHES = 13u
        const val UNKNOWN_14 = 14u
        const val SUPPORTS_STRIPPED_OBJECT = 15u
        const val REFACTORED_CLASS_ID = 16u
        const val REFACTOR_TYPE_DATA = 17u
//        const val RefactorShareableTypeTreeData = 18u
        const val TYPE_TREE_NODE_WITH_TYPE_FLAGS = 19u
        const val SUPPORTS_REF_OBJECT = 20u
        const val STORES_TYPE_DEPENDENCIES = 21u
        const val LARGE_FILE_SUPPORT = 22u
    }
}

internal class FileIdentifier private constructor(
    val type: Int,
    val path: String,
    val name: String
) {
    companion object {
        fun fromPath(type: Int, path: String) = FileIdentifier(type, path, File(path).name)
        fun fromName(type: Int, name: String) = FileIdentifier(type, "", name)
    }
}

internal class SerializedFile(
    internal val reader: EndianBinaryReader,
    override val parent: FileNode,
    override val name: String
): AbstractFile {
    class Header(
        var metadataSize: UInt,
        var fileSize: Long,
        val version: UInt,
        var dataOffset: Long,
        var endianess: UByte = 0u,
    )

    class ObjectIdentifier(
        val serializedFileIndex: Int,
        val identifierInFile: Long
    )

    private val header = Header(
        metadataSize = reader.readUInt32(),
        fileSize = reader.readUInt32().toLong(),
        version = reader.readUInt32(),
        dataOffset = reader.readUInt32().toLong()
    )

    private val unityVersion: UnityVersion
    private val buildTarget: BuildTarget
    private val enableTypeTree: Boolean
    private val bigIDEnabled: Int
    private val userInformation: String

    private val types: Array<SerializedTypeImpl>
    val objectMetadataMap: Map<Long, UnityObjectMetadataImpl>
    private val scriptTypes: Array<ObjectIdentifier>
    val externals = mutableListOf<FileIdentifier>()
    private val refTypes: Array<SerializedTypeImpl>

    init {
        with(header) {
            if (version >= FormatVersion.UNKNOWN_9) {
                endianess = reader.readUInt8()
                reader.skip(3)     //hReserved = reader.read(3)
            } else {
                reader.position = fileSize - metadataSize.toLong()
                endianess = reader.readUInt8()
            }
            if (version >= FormatVersion.LARGE_FILE_SUPPORT) {
                metadataSize = reader.readUInt32()
                fileSize = reader.readInt64()
                dataOffset = reader.readInt64()
                reader.skip(8)     //unknown: Long
            }
        }
        if (header.endianess == 0u.toUByte()) {
            reader.endian = ByteOrder.LITTLE_ENDIAN
        }
        unityVersion = if (header.version >= FormatVersion.UNKNOWN_7) {
            UnityVersion(reader.readNullString())
        } else {
            UnityVersion(2, 5, 0, BuildType.FINAL, 5)
        }
        buildTarget = if (header.version >= FormatVersion.UNKNOWN_8) {
            BuildTarget.of(reader.readInt32())
        } else {
            BuildTarget.UnknownPlatform
        }
        enableTypeTree = if (header.version >= FormatVersion.HAS_TYPE_TREE_HASHES) reader.readBool() else true
        types = reader.readArrayOf {
            readSerializedType(false)
        }
        bigIDEnabled = if (header.version in FormatVersion.UNKNOWN_7 ..< FormatVersion.UNKNOWN_14) {
            reader.readInt32()
        } else 0
        objectMetadataMap = reader.readArrayOf {
            val mPathID = if (bigIDEnabled != 0) {
                readInt64()
            } else if (header.version < FormatVersion.UNKNOWN_14) {
                readInt32().toLong()
            } else {
                alignStream()
                readInt64()
            }
            val byteStart = if (header.version >= FormatVersion.LARGE_FILE_SUPPORT) {
                readInt64()
            } else {
                readUInt32().toLong()
            } + header.dataOffset
            val byteSize = readUInt32()
            val typeID = readInt32()
            val classID: Int; val serialisedType: SerializedTypeImpl?
            if (header.version < FormatVersion.REFACTORED_CLASS_ID) {
                classID = readUInt16().toInt()
                serialisedType = types.find { it.classID == typeID }
            } else {
                with(types[typeID]) {
                    serialisedType = this
                    classID = this.classID
                }
            }
            val isDestroyed: UShort = if (header.version < FormatVersion.HAS_SCRIPT_TYPE_INDEX) readUInt16() else 0u
            if (header.version in FormatVersion.HAS_SCRIPT_TYPE_INDEX ..< FormatVersion.REFACTOR_TYPE_DATA) {
                if (serialisedType != null) {
                    serialisedType.scriptTypeIndex = readInt16()
                }
            }
            val stripped: UByte = if (
                header.version == FormatVersion.SUPPORTS_STRIPPED_OBJECT ||
                header.version == FormatVersion.REFACTORED_CLASS_ID
            ) readUInt8() else 0u
            UnityObjectMetadataImpl(
                this@SerializedFile, byteStart, byteSize, typeID,
                classID, isDestroyed, stripped, mPathID, serialisedType
            )
        }.associateBy { it.m_PathID }
        scriptTypes = if (header.version >= FormatVersion.HAS_SCRIPT_TYPE_INDEX) {
            reader.readArrayOf {
                ObjectIdentifier(
                    serializedFileIndex = readInt32(),
                    identifierInFile = if (header.version < FormatVersion.UNKNOWN_14) {
                        readInt32().toLong()
                    } else {
                        alignStream()
                        readInt64()
                    }
                )
            }
        } else emptyArray()
        externals.addAll(
            reader.readArrayOf {
                if (header.version >= FormatVersion.UNKNOWN_6) readNullString()
                val type = if (header.version >= FormatVersion.UNKNOWN_5) {
                    //guid: UUID (byte[16])
                    skip(16)
                    readInt32()
                } else 0
                val path = readNullString()
                FileIdentifier.fromPath(type, path)
            }
        )
        refTypes = if (header.version >= FormatVersion.SUPPORTS_REF_OBJECT) {
            reader.readArrayOf { readSerializedType(true) }
        } else emptyArray()
        userInformation = if (header.version >= FormatVersion.UNKNOWN_5) {
            reader.readNullString()
        } else ""
        root.objectMap.putAll(objectMetadataMap)
    }

    private fun readSerializedType(isRefType: Boolean): SerializedTypeImpl {
        val classID = reader.readInt32()
        val isStrippedType = if (header.version >= FormatVersion.REFACTORED_CLASS_ID) reader.readBool() else false
        val scriptTypeIndex = if (header.version >= FormatVersion.REFACTOR_TYPE_DATA) reader.readInt16() else 0
        val scriptID = ByteArray(16)
        val oldTypeHash = ByteArray(16)
        val typeTree = TypeTreeImpl()
        var className = ""
        var nameSpace = ""
        var asmName = ""
        var typeDependencies: IntArray = intArrayOf()

        if (header.version >= FormatVersion.HAS_TYPE_TREE_HASHES) {
            if (isRefType && scriptTypeIndex >= 0) {
                reader.read(scriptID)
            } else if (
                (header.version < FormatVersion.REFACTORED_CLASS_ID && classID < 0) ||
                (header.version >= FormatVersion.REFACTORED_CLASS_ID && classID == 114)
            ) {
                reader.read(scriptID)
            }
            reader.read(oldTypeHash)
        }
        if (enableTypeTree) {
            if (
                header.version >= FormatVersion.UNKNOWN_12 ||
                header.version == FormatVersion.UNKNOWN_10
            ) {
                typeTreeBlobRead(typeTree)
            } else {
                readTypeTree(typeTree)
            }
            if (header.version >= FormatVersion.STORES_TYPE_DEPENDENCIES) {
                if (isRefType) {
                    className = reader.readNullString()
                    nameSpace = reader.readNullString()
                    asmName = reader.readNullString()
                } else {
                    typeDependencies = reader.readInt32Array()
                }
            }
        }
        return SerializedTypeImpl(
            classID, isStrippedType, scriptTypeIndex, typeTree, scriptID,
            oldTypeHash, typeDependencies, className, nameSpace, asmName
        )
    }

    private fun readNodeString(reader: EndianBinaryReader, value: UInt): String {
        if (value.and(0x80000000u) == 0u) {
            reader.position = value.toLong()
            return reader.readNullString()
        }
        val offset = value.and(0x7FFFFFFFu)
        return commonString[offset] ?: offset.toString()
    }

    private fun typeTreeBlobRead(tree: TypeTreeImpl) {
        val nodeCount = reader.readInt32()
        val stringBufferSize = reader.readInt32()
        val hasRefTypeHash = header.version >= FormatVersion.TYPE_TREE_NODE_WITH_TYPE_FLAGS
        val typeOffsetList = mutableListOf<UInt>()
        val nameOffsetList = mutableListOf<UInt>()
        for (i in 0 ..< nodeCount) {
            val version = reader.readUInt16().toInt()
            val level = reader.readUInt8().toInt()
            val typeFlags = reader.readUInt8().toInt()
            val typeStrOffset = reader.readUInt32()
            val nameStrOffset = reader.readUInt32()
            val byteSize = reader.readInt32()
            val index = reader.readInt32()
            val metaFlag = reader.readInt32()
            val refTypeHash = if (hasRefTypeHash) reader.readUInt64() else 0u
            tree.nodes.add(
                TypeTreeNodeImpl(
                    byteSize, index, typeFlags, version,
                    metaFlag, level, refTypeHash
                )
            )
            typeOffsetList.add(typeStrOffset)
            nameOffsetList.add(nameStrOffset)
        }
        EndianByteArrayReader(reader.read(stringBufferSize)).use {
            for (i in 0 ..< nodeCount) {
                val node = tree.nodes[i]
                node.type = readNodeString(it, typeOffsetList[i])
                node.name = readNodeString(it, nameOffsetList[i])
            }
        }
    }

    private fun readTypeTree(tree: TypeTreeImpl, level: Int = 0) {
        val type = reader.readNullString()
        val name = reader.readNullString()
        val byteSize = reader.readInt32()
        if (header.version == FormatVersion.UNKNOWN_2) reader.skip(4)   //variableCount
        val index = if (header.version != FormatVersion.UNKNOWN_3) reader.readInt32() else 0
        val typeFlags = reader.readInt32()
        val version = reader.readInt32()
        val metaFlag = if (header.version != FormatVersion.UNKNOWN_3) reader.readInt32() else 0
        tree.nodes.add(
            TypeTreeNodeImpl(
                byteSize, index, typeFlags, version, metaFlag, level,
                0u, type, name
            )
        )
        val childrenCount = reader.readInt32()
        for (i in 0 ..< childrenCount) {
            readTypeTree(tree, level + 1)
        }
    }

    companion object {
        private val commonString = mapOf(
            0u to "AABB", 5u to "AnimationClip", 19u to "AnimationCurve", 34u to "AnimationState",
            49u to "Array", 55u to "Base", 60u to "BitField", 69u to "bitset", 76u to "bool",
            81u to "char", 86u to "ColorRGBA", 96u to "Component", 106u to "data", 111u to "deque",
            117u to "double", 124u to "dynamic_array", 138u to "FastPropertyName", 155u to "first",
            161u to "float", 167u to "Font", 172u to "GameObject", 183u to "Generic Mono", 196u to "GradientNEW",
            208u to "GUID", 213u to "GUIStyle", 222u to "int", 226u to "list", 231u to "long long",
            241u to "map", 245u to "Matrix4x4f", 256u to "MdFour", 263u to "MonoBehaviour", 277u to "MonoScript",
            288u to "m_ByteSize", 299u to "m_Curve", 307u to "m_EditorClassIdentifier", 331u to "m_EditorHideFlags",
            349u to "m_Enabled", 359u to "m_ExtensionPtr", 374u to "m_GameObject", 387u to "m_Index",
            395u to "m_IsArray", 405u to "m_IsStatic", 416u to "m_MetaFlag", 427u to "m_Name",
            434u to "m_ObjectHideFlags", 452u to "m_PrefabInternal", 469u to "m_PrefabParentObject",
            490u to "m_Script", 499u to "m_StaticEditorFlags", 519u to "m_Type", 526u to "m_Version",
            536u to "Object", 543u to "pair", 548u to "PPtr<Component>", 564u to "PPtr<GameObject>",
            581u to "PPtr<Material>", 596u to "PPtr<MonoBehaviour>", 616u to "PPtr<MonoScript>",
            633u to "PPtr<Object>", 646u to "PPtr<Prefab>", 659u to "PPtr<Sprite>", 672u to "PPtr<TextAsset>",
            688u to "PPtr<Texture>", 702u to "PPtr<Texture2D>", 718u to "PPtr<Transform>",
            734u to "Prefab", 741u to "Quaternionf", 753u to "Rectf", 759u to "RectInt", 767u to "RectOffset",
            778u to "second", 785u to "set", 789u to "short", 795u to "size", 800u to "SInt16", 807u to "SInt32",
            814u to "SInt64", 821u to "SInt8", 827u to "staticvector", 840u to "string", 847u to "TextAsset",
            857u to "TextMesh", 866u to "Texture", 874u to "Texture2D", 884u to "Transform", 894u to "TypelessData",
            907u to "UInt16", 914u to "UInt32", 921u to "UInt64", 928u to "UInt8", 934u to "unsigned int",
            947u to "unsigned long long", 966u to "unsigned short", 981u to "vector", 988u to "Vector2f",
            997u to "Vector3f", 1006u to "Vector4f", 1015u to "m_ScriptingClassIdentifier", 1042u to "Gradient",
            1051u to "Type*", 1057u to "int2_storage", 1070u to "int3_storage", 1083u to "BoundsInt",
            1093u to "m_CorrespondingSourceObject", 1121u to "m_PrefabInstance", 1138u to "m_PrefabAsset",
            1152u to "FileSize", 1161u to "Hash128", 1169u to "RenderingLayerMask"
        )
    }
}