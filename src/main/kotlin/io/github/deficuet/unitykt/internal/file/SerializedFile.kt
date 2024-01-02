package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.internal.impl.*
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayReader
import io.github.deficuet.unitykt.util.readArrayOf
import java.io.File
import java.nio.ByteOrder

internal class FormatVersion private constructor() {
    companion object {
//        const val Unsupported = 1u
        const val Unknown_2 = 2u
        const val Unknown_3 = 3u
        const val Unknown_5 = 5u
        const val Unknown_6 = 6u
        const val Unknown_7 = 7u
        const val Unknown_8 = 8u
        const val Unknown_9 = 9u
        const val Unknown_10 = 10u
        const val HasScriptTypeIndex = 11u
        const val Unknown_12 = 12u
        const val HasTypeTreeHashes = 13u
        const val Unknown_14 = 14u
        const val SupportsStrippedObject = 15u
        const val RefactoredClassId = 16u
        const val RefactorTypeData = 17u
//        const val RefactorShareableTypeTreeData = 18u
        const val TypeTreeNodeWithTypeFlags = 19u
        const val SupportsRefObject = 20u
        const val StoresTypeDependencies = 21u
        const val LargeFilesSupport = 22u
    }
}

internal class BuildType(private val type: String) {
//    val isAlpha get() = type == "a"
    val isPatch get() = type == "p"
}

internal data class ObjectInfo(
    val byteStart: Long,
    val byteSize: UInt,
    val typeID: Int,
    val classID: Int,
    val isDestroyed: UShort,
    val stripped: UByte,
    val mPathID: Long,
    val serializedType: SerializedType?
) {
    val type = ClassIDType.of(classID)
}

internal class SerializedFile(
    internal val reader: EndianBinaryReader,
    override val bundleParent: FileNode,
    override val name: String
): AssetFile {
    class FileIdentifier private constructor(
        val path: String,
        val name: String
    ) {
        companion object {
            fun fromPath(path: String) = FileIdentifier(path, File(path).name)
            fun fromName(name: String) = FileIdentifier("", name)
        }
    }

    data class ObjectIdentifier(
        val serializedFileIndex: Int,
        val identifierInFile: Long
    )

    data class SerializedTypeTreeNodeInternal(
        val byteSize: Int,
        val index: Int,
        val typeFlags: Int,
        val version: Int,
        val metaFlag: Int,
        val level: Int,
        val typeStrOffset: UInt,
        val nameStrOffset: UInt
    )

    //Header
    private var hMetadataSize = reader.readUInt32()
    private var hFileSize = reader.readUInt32().toLong()
    val hVersion = reader.readUInt32()
    private var hDataOffset = reader.readUInt32().toLong()
    private val hEndian: UByte
//    private val hReserved: ByteArray

    private var unityVersion = "2.5.0.f5"

    private val enableTypeTree: Boolean
    private var bigIDEnabled = 0
    private val objectInfoArray: Array<ObjectInfo>
    private val scriptTypes: Array<ObjectIdentifier>
    private val refTypes: Array<SerializedType>
    private val userInformation: String
    private val types: Array<SerializedType>

    var version: IntArray = intArrayOf(0, 0, 0, 0)
    var targetPlatform = BuildTarget.UnknownPlatform
    var buildType = BuildType("")
    val externals = mutableListOf<FileIdentifier>()

    val objectMap = mutableMapOf<Long, UnityObject>()

    init {
        if (hVersion >= FormatVersion.Unknown_9) {
            hEndian = reader.readUInt8()
            reader.skip(3)     //hReserved = reader.read(3)
        } else {
            reader.position = hFileSize - hMetadataSize.toLong()
            hEndian = reader.readUInt8()
        }
        if (hVersion >= FormatVersion.LargeFilesSupport) {
            hMetadataSize = reader.readUInt32()
            hFileSize = reader.readInt64()
            hDataOffset = reader.readInt64()
            reader.skip(8)     //unknown: Long
        }
        if (hEndian == 0u.toUByte()) reader.endian = ByteOrder.LITTLE_ENDIAN
        if (hVersion >= FormatVersion.Unknown_7) {
            unityVersion = reader.readNullString()
            buildType = buildTypeRegex.findAll(unityVersion).iterator().let {
                BuildType(if (it.hasNext()) it.next().value else "")
            }
            version = versionSplitRegex.split(unityVersion).let { digits ->
                IntArray(digits.size) { index -> digits[index].toInt() }
            }
        }
        if (hVersion >= FormatVersion.Unknown_8) {
            val targetPlatformID = reader.readInt32()
            if (BuildTarget.isDefined(targetPlatformID)) {
                targetPlatform = BuildTarget.values().first { it.id == targetPlatformID }
            }
        }
        enableTypeTree = if (hVersion >= FormatVersion.HasTypeTreeHashes) reader.readBool() else true
        types = reader.readArrayOf {
            readSerializedType(false)
        }
        if (hVersion >= FormatVersion.Unknown_7 && hVersion < FormatVersion.Unknown_14) {
            bigIDEnabled = reader.readInt32()
        }
        objectInfoArray = reader.readArrayOf {
            val mPathID = if (bigIDEnabled != 0) {
                readInt64()
            } else if (hVersion < FormatVersion.Unknown_14) {
                readInt32().toLong()
            } else {
                alignStream()
                readInt64()
            }
            var byteStart = if (hVersion >= FormatVersion.LargeFilesSupport) {
                readInt64()
            } else {
                readUInt32().toLong()
            }
            byteStart += hDataOffset
            val byteSize = readUInt32()
            val typeID = readInt32()
            val classID: Int; val serialisedType: SerializedType?
            if (hVersion < FormatVersion.RefactoredClassId) {
                classID = readUInt16().toInt()
                serialisedType = types.find { it.classID == typeID }
            } else {
                with(types[typeID]) {
                    serialisedType = this
                    classID = this.classID
                }
            }
            val isDestroyed: UShort = if (hVersion < FormatVersion.HasScriptTypeIndex) readUInt16() else 0u
            if (hVersion >= FormatVersion.HasScriptTypeIndex && hVersion < FormatVersion.RefactorTypeData) {
                serialisedType?.scriptTypeIndex = readInt16()
            }
            val stripped: UByte = if (
                hVersion == FormatVersion.SupportsStrippedObject ||
                hVersion == FormatVersion.RefactoredClassId
            ) readUInt8() else 0u
            ObjectInfo(
                byteStart, byteSize, typeID, classID, isDestroyed,
                stripped, mPathID, serialisedType
            )
        }
        scriptTypes = if (hVersion >= FormatVersion.HasScriptTypeIndex) {
            reader.readArrayOf {
                ObjectIdentifier(
                    serializedFileIndex = readInt32(),
                    identifierInFile = if (hVersion < FormatVersion.Unknown_14) {
                        readInt32().toLong()
                    } else {
                        alignStream()
                        readInt64()
                    }
                )
            }
        } else emptyArray()
        reader.readArrayOf {
            if (hVersion >= FormatVersion.Unknown_6) readNullString()
            if (hVersion >= FormatVersion.Unknown_5) {
                //guid: UUID (byte[16])
                //type: Int32
                skip(20)
            }
            externals.add(FileIdentifier.fromPath(readNullString()))
        }
        refTypes = if (hVersion >= FormatVersion.SupportsRefObject) {
            reader.readArrayOf { readSerializedType(true) }
        } else emptyArray()
        userInformation = if (hVersion >= FormatVersion.Unknown_5) {
            reader.readNullString()
        } else ""
        //region readObjects
        for (info in objectInfoArray) {
            val obj = when (info.type) {
                ClassIDType.Animation -> AnimationImpl(this, info)
                ClassIDType.AnimationClip -> AnimationClipImpl(this, info)
                ClassIDType.Animator -> AnimatorImpl(this, info)
                ClassIDType.AnimatorController -> AnimatorControllerImpl(this, info)
                ClassIDType.AnimatorOverrideController -> AnimatorOverrideControllerImpl(this, info)
                ClassIDType.AssetBundle -> AssetBundleImpl(this, info)
                ClassIDType.AudioClip -> AudioClipImpl(this, info)
                ClassIDType.Avatar -> AvatarImpl(this, info)
                ClassIDType.Canvas -> CanvasImpl(this, info)
//                ClassIDType.Font -> Font(this, info)  TODO
                ClassIDType.GameObject -> GameObjectImpl(this, info)
                ClassIDType.Material -> MaterialImpl(this, info)
                ClassIDType.Mesh -> MeshImpl(this, info)
                ClassIDType.MeshFilter -> MeshFilterImpl(this, info)
                ClassIDType.MeshRenderer -> MeshRendererImpl(this, info)
                ClassIDType.MonoBehaviour -> MonoBehaviourImpl(this, info)
                ClassIDType.MonoScript -> MonoScriptImpl(this, info)
                ClassIDType.MovieTexture -> MovieTextureImpl(this, info)
                ClassIDType.PlayerSettings -> PlayerSettingsImpl(this, info)
                ClassIDType.RectTransform -> RectTransformImpl(this, info)
                ClassIDType.Shader -> ShaderImpl(this, info)
                ClassIDType.SkinnedMeshRenderer -> SkinnedMeshRendererImpl(this, info)
                ClassIDType.Sprite -> SpriteImpl(this, info)
                ClassIDType.SpriteAtlas -> SpriteAtlasImpl(this, info)
                ClassIDType.TextAsset -> TextAssetImpl(this, info)
                ClassIDType.Texture2D -> Texture2DImpl(this, info)
                ClassIDType.Transform -> TransformImpl(this, info)
                ClassIDType.VideoClip -> VideoClipImpl(this, info)
                ClassIDType.ResourceManager -> ResourceManagerImpl(this, info)
                else -> UnityObjectImpl(this, info)
            }
            objectMap[obj.mPathID] = obj
        }
        root.objectMap.putAll(objectMap)
        //endregion
    }

    private fun readSerializedType(isRefType: Boolean): SerializedType {
        val classID = reader.readInt32()
        var isStrippedType = false
        var scriptTypeIndex: Short = -1
        var scriptID: ByteArray = byteArrayOf()
        var oldTypeHash: ByteArray = byteArrayOf()
        var typeTree = SerializedType.Tree()
        var className = ""
        var nameSpace = ""
        var asmName = ""
        var typeDependencies: IntArray = intArrayOf()
        if (hVersion >= FormatVersion.RefactoredClassId) isStrippedType = reader.readBool()
        if (hVersion >= FormatVersion.RefactorTypeData) scriptTypeIndex = reader.readInt16()
        if (hVersion >= FormatVersion.HasTypeTreeHashes) {
            if (isRefType && scriptTypeIndex >= 0) scriptID = reader.read(16)
            else if ((hVersion < FormatVersion.RefactoredClassId && classID < 0) ||
                (hVersion >= FormatVersion.RefactoredClassId && classID == 114)) {
                scriptID = reader.read(16)
            }
            oldTypeHash = reader.read(16)
        }
        if (enableTypeTree) {
            typeTree = if (hVersion >= FormatVersion.Unknown_12 || hVersion == FormatVersion.Unknown_10) {
                typeTreeBlobRead()
            } else {
                readTypeTree()
            }
            if (hVersion >= FormatVersion.StoresTypeDependencies) {
                if (isRefType) {
                    className = reader.readNullString()
                    nameSpace = reader.readNullString()
                    asmName = reader.readNullString()
                } else {
                    typeDependencies = reader.readInt32Array()
                }
            }
        }
        return SerializedType(
            classID,
            isStrippedType,
            scriptTypeIndex,
            typeTree,
            scriptID,
            oldTypeHash,
            typeDependencies,
            className,
            nameSpace,
            asmName
        )
    }

    private fun EndianBinaryReader.readNodeString(value: UInt): String {
        if (value.and(0x80000000u) == 0u) {
            position = value.toLong()
            return readNullString()
        }
        val offset = value.and(0x7FFFFFFFu)
        return commonString[offset] ?: offset.toString()
    }

    private fun typeTreeBlobRead(): SerializedType.Tree {
        val nodeCount = reader.readInt32()
        val stringBufferSize = reader.readInt32()
        val nodeList = mutableListOf<SerializedType.TreeNode>()
        val nodeListInternal = mutableListOf<SerializedTypeTreeNodeInternal>()
        for (i in 0 until nodeCount) {
            nodeListInternal.add(
                SerializedTypeTreeNodeInternal(
                    version = reader.readUInt16().toInt(),
                    level = reader.readUInt8().toInt(),
                    typeFlags = reader.readUInt8().toInt(),
                    typeStrOffset = reader.readUInt32(),
                    nameStrOffset = reader.readUInt32(),
                    byteSize = reader.readInt32(),
                    index = reader.readInt32(),
                    metaFlag = reader.readInt32()
                )
            )
            if (hVersion >= FormatVersion.TypeTreeNodeWithTypeFlags) {
                reader.skip(8)     //refTypeHash: ULong
            }
        }
        val stringBuffer = reader.read(stringBufferSize)
        EndianByteArrayReader(stringBuffer).use {
            for (node in nodeListInternal) {
                nodeList.add(
                    SerializedType.TreeNode(
                        node,
                        type = it.readNodeString(node.typeStrOffset),
                        name = it.readNodeString(node.nameStrOffset)
                    )
                )
            }
        }
        return SerializedType.Tree(nodeList)
    }

    private fun readTypeTree(): SerializedType.Tree {
        val newTree = SerializedType.Tree()
        val levelStack = mutableListOf(intArrayOf(0, 1))
        while (levelStack.isNotEmpty()) {
            val (level, count) = levelStack.last()
            if (count == 1) {
                levelStack.removeLast()
            } else {
                levelStack.last()[1] -= 1
            }
            val type = reader.readNullString()
            val name = reader.readNullString()
            val byteSize = reader.readInt32()
            if (hVersion == FormatVersion.Unknown_2) reader.skip(4)   //variableCount
            val index = if (hVersion != FormatVersion.Unknown_3) reader.readInt32() else 0
            val typeFlags = reader.readInt32()
            val version = reader.readInt32()
            val metaFlag = if (hVersion != FormatVersion.Unknown_3) reader.readInt32() else 0
            newTree.nodes.add(
                SerializedType.TreeNode(
                    type, name, byteSize, index, typeFlags, version, metaFlag, level
                )
            )
            val childrenCount = reader.readInt32()
            if (childrenCount > 0) levelStack.add(intArrayOf(level + 1, childrenCount))
        }
        return newTree
    }

    companion object {
        private val buildTypeRegex = Regex("""([^\d.])""")
        private val versionSplitRegex = Regex("""\D""")
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
            1152u to "FileSize", 1161u to "Hash128"
        )
    }
}