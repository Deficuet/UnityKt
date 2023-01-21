package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.data.*
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayReader
import java.io.File
import java.nio.ByteOrder

class FormatVersion private constructor() {
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

class BuildType(private val type: String) {
//    val isAlpha get() = type == "a"
    val isPatch get() = type == "p"
}

@Suppress("EnumEntryName")
enum class BuildTarget(val id: Int) {
    NoTarget(-2),
    AnyPlayer(-1),
    ValidPlayer(1),
    StandaloneOSX(2),
    StandaloneOSXPPC(3),
    StandaloneOSXIntel(4),
    StandaloneWindows(5),
    WebPlayer(6),
    WebPlayerStreamed(7),
    Wii(8),
    iOS(9),
    PS3(10),
    XBOX360(11),
    Broadcom(12),
    Android(13),
    StandaloneGLESEmu(14),
    StandaloneGLES20Emu(15),
    NaCl(16),
    StandaloneLinux(17),
    FlashPlayer(18),
    StandaloneWindows64(19),
    WebGL(20),
    WSAPlayer(21),
    StandaloneLinux64(24),
    StandaloneLinuxUniversal(25),
    WP8Player(26),
    StandaloneOSXIntel64(27),
    BlackBerry(28),
    Tizen(29),
    PSP2(30),
    PS4(31),
    PSM(32),
    XboxOne(33),
    SamsungTV(34),
    N3DS(35),
    WiiU(36),
    tvOS(37),
    Switch(38),
    Lumin(39),
    Stadia(40),
    CloudRendering(41),
    GameCoreXboxSeries(42),
    GameCoreXboxOne(43),
    PS5(44),
    EmbeddedLinux(45),
    QNX(46),

    UnknownPlatform(9999);

    companion object {
        fun isDefined(v: Int) = values().any { it.id == v }
    }
}

data class ObjectInfo(
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

class SerializedFile(
    internal val reader: EndianBinaryReader,
    override val bundleParent: AssetBundleFile,
    override val name: String
): RawAssetFile() {
    data class Header(
        val metadataSize: UInt = 0u,
        val fileSize: Long = 0,
        val version: UInt = 0u,
        val dataOffset: Long = 0,
        val endian: UByte = 0u
    )

    data class FileIdentifier(
        val guid: ByteArray,
        val type: Int,
        val path: String
    ) {
        val name: String by lazy { File(path).name }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FileIdentifier

            if (!guid.contentEquals(other.guid)) return false
            if (type != other.type) return false
            if (path != other.path) return false

            return true
        }

        override fun hashCode(): Int {
            var result = guid.contentHashCode()
            result = 31 * result + type
            result = 31 * result + path.hashCode()
            return result
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
    private var hMetadataSize = reader.readUInt()
    private var hFileSize = reader.readUInt().toLong()
    private val hVersion = reader.readUInt()
    private var hDataOffset = reader.readUInt().toLong()
    private val hEndian: UByte
//    private val hReserved: ByteArray

    private var unityVersion = "2.5.0.f5"

    private val enableTypeTree: Boolean
    private var bigIDEnabled = 0
    private val objectInfoList: List<ObjectInfo>
    private val scriptTypes = mutableListOf<ObjectIdentifier>()
    private val refTypes = mutableListOf<SerializedType>()
    private val userInformation: String
    private val types: List<SerializedType>

    val header: Header get() = Header(hMetadataSize, hFileSize, hVersion, hDataOffset, hEndian)
    var version: IntArray = intArrayOf(0, 0, 0, 0)
        private set
    var targetPlatform = BuildTarget.UnknownPlatform
        private set
    var buildType = BuildType("")
        private set
    val externals: List<FileIdentifier>
    val objects: Map<Long, Object>
    val objectList: Collection<Object> get() = objects.values

    init {
        if (hVersion >= FormatVersion.Unknown_9) {
            hEndian = reader.readByte()
            reader += 3     //hReserved = reader.read(3)
        } else {
            reader.position = hFileSize - hMetadataSize.toLong()
            hEndian = reader.readByte()
        }
        if (hVersion >= FormatVersion.LargeFilesSupport) {
            hMetadataSize = reader.readUInt()
            hFileSize = reader.readLong()
            hDataOffset = reader.readLong()
            reader += 8     //unknown
        }
        if (hEndian == 0u.toUByte()) reader.resetEndian(ByteOrder.LITTLE_ENDIAN)
        if (hVersion >= FormatVersion.Unknown_7) {
            unityVersion = reader.readStringUntilNull()
            buildType = buildTypeRegex.findAll(unityVersion).iterator().let {
                BuildType(
                    if (it.hasNext()) it.next().value
                    else ""
                )
            }
            version = versionSplitRegex.split(unityVersion).map { it.toInt() }.toIntArray()
        }
        if (hVersion >= FormatVersion.Unknown_8) {
            val targetPlatformID = reader.readInt()
            if (BuildTarget.isDefined(targetPlatformID)) {
                targetPlatform = BuildTarget.values().first { it.id == targetPlatformID }
            }
        }
        enableTypeTree = if (hVersion >= FormatVersion.HasTypeTreeHashes) reader.readBool() else true
        val typeCount = reader.readInt()
        val typesList = mutableListOf<SerializedType>()
        for (i in 0 until typeCount) {
            typesList.add(readSerializedType(false))
        }
        types = typesList
        if (hVersion in with(FormatVersion) { Unknown_7 until Unknown_14 }) {
            bigIDEnabled = reader.readInt()
        }
        val objectCount = reader.readInt()
        val objectInfoList = mutableListOf<ObjectInfo>()
        for (j in 0 until objectCount) {
            val mPathID = if (bigIDEnabled != 0) {
                reader.readLong()
            } else if (hVersion < FormatVersion.Unknown_14) {
                reader.readInt().toLong()
            } else {
                reader.alignStream()
                reader.readLong()
            }
            var byteStart = if (hVersion >= FormatVersion.LargeFilesSupport) {
                reader.readLong()
            } else reader.readUInt().toLong()
            byteStart += hDataOffset
            val byteSize = reader.readUInt()
            val typeID = reader.readInt()
            val classID: Int; val serialisedType: SerializedType?
            if (hVersion < FormatVersion.RefactoredClassId) {
                classID = reader.readUShort().toInt()
                serialisedType = types.find { it.classID == typeID }
            } else {
                with(types[typeID]) {
                    serialisedType = this
                    classID = this.classID
                }
            }
            var isDestoryed: UShort = 0u
            if (hVersion < FormatVersion.HasScriptTypeIndex) {
                isDestoryed = reader.readUShort()
            }
            if (hVersion in with(FormatVersion) { HasScriptTypeIndex until RefactorTypeData } ) {
                serialisedType?.scriptTypeIndex = reader.readShort()
            }
            var stripped: UByte = 0u
            if (hVersion == FormatVersion.SupportsStrippedObject ||
                hVersion == FormatVersion.RefactoredClassId) {
                stripped = reader.readByte()
            }
            objectInfoList.add(
                ObjectInfo(
                    byteStart, byteSize, typeID, classID, isDestoryed,
                    stripped, mPathID, serialisedType
                )
            )
        }
        this.objectInfoList = objectInfoList
        if (hVersion >= FormatVersion.HasScriptTypeIndex) {
            val scriptCount = reader.readInt()
            for (k in 0 until scriptCount) {
                scriptTypes.add(
                    ObjectIdentifier(
                        serializedFileIndex = reader.readInt(),
                        identifierInFile = if (hVersion < FormatVersion.Unknown_14) {
                            reader.readInt().toLong()
                        } else {
                            reader.alignStream()
                            reader.readLong()
                        }
                    )
                )
            }
        }
        val externalsCount = reader.readInt()
        val externals = mutableListOf<FileIdentifier>()
        for (l in 0 until externalsCount) {
            if (hVersion >= FormatVersion.Unknown_6) reader.readStringUntilNull()
            var guid = ByteArray(16); var type = 0
            if (hVersion >= FormatVersion.Unknown_5) {
                guid = reader.read(16)
                type = reader.readInt()
            }
            externals.add(
                FileIdentifier(
                    guid, type,
                    reader.readStringUntilNull()
                )
            )
        }
        this.externals = externals
        if (hVersion >= FormatVersion.SupportsRefObject) {
            val refTypesCount = reader.readInt()
            for (m in 0 until refTypesCount) {
                refTypes.add(readSerializedType(true))
            }
        }
        userInformation = if (hVersion >= FormatVersion.Unknown_5) {
            reader.readStringUntilNull()
        } else ""
        //region readObjects
        val objectMap = mutableMapOf<Long, Object>()
        for (info in objectInfoList) {
            val obj = when (info.type) {
                ClassIDType.Animation -> Animation(this, info)
                ClassIDType.AnimationClip -> AnimationClip(this, info)
                ClassIDType.Animator -> Animator(this, info)
                ClassIDType.AnimatorController -> AnimatorController(this, info)
                ClassIDType.AnimatorOverrideController -> AnimatorOverrideController(this, info)
                ClassIDType.AssetBundle -> AssetBundle(this, info)
                ClassIDType.AudioClip -> AudioClip(this, info)
                ClassIDType.Avatar -> Avatar(this, info)
                ClassIDType.Canvas -> Canvas(this, info)
                ClassIDType.Font -> Font(this, info)
                ClassIDType.GameObject -> GameObject(this, info)
                ClassIDType.Material -> Material(this, info)
                ClassIDType.Mesh -> Mesh(this, info)
                ClassIDType.MeshFilter -> MeshFilter(this, info)
                ClassIDType.MeshRenderer -> MeshRenderer(this, info)
                ClassIDType.MonoBehaviour -> MonoBehaviour(this, info)
                ClassIDType.MonoScript -> MonoScript(this, info)
                ClassIDType.MovieTexture -> MovieTexture(this, info)
                ClassIDType.PlayerSettings -> PlayerSetting(this, info)
                ClassIDType.RectTransform -> RectTransform(this, info)
                ClassIDType.Shader -> Shader(this, info)
                ClassIDType.SkinnedMeshRenderer -> SkinnedMeshRenderer(this, info)
                ClassIDType.Sprite -> Sprite(this, info)
                ClassIDType.SpriteAtlas -> SpriteAtlas(this, info)
                ClassIDType.TextAsset -> TextAsset(this, info)
                ClassIDType.Texture2D -> Texture2D(this, info)
                ClassIDType.Transform -> Transform(this, info)
                ClassIDType.VideoClip -> VideoClip(this, info)
                ClassIDType.ResourceManager -> ResourceManager(this, info)
                else -> Object(this, info)
            }
            objectMap[obj.mPathID] = obj
        }
        objects = objectMap
        root.objects.putAll(objects)
        objectInfoList.clear()
        //endregion
    }

    private fun readSerializedType(isRefType: Boolean): SerializedType {
        val classID = reader.readInt()
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
        if (hVersion >= FormatVersion.RefactorTypeData) scriptTypeIndex = reader.readShort()
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
        }
        if (hVersion >= FormatVersion.StoresTypeDependencies) {
            if (isRefType) {
                className = reader.readStringUntilNull()
                nameSpace = reader.readStringUntilNull()
                asmName = reader.readStringUntilNull()
            } else {
                typeDependencies = reader.readNextIntArray()
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

    private fun typeTreeBlobRead(): SerializedType.Tree {
        val nodeCount = reader.readInt()
        val stringBufferSize = reader.readInt()
        val nodeList = mutableListOf<SerializedType.TreeNode>()
        val nodeListInternal = mutableListOf<SerializedTypeTreeNodeInternal>()
        for (i in 0 until nodeCount) {
            nodeListInternal.add(
                SerializedTypeTreeNodeInternal(
                    version = reader.readUShort().toInt(),
                    level = reader.readByte().toInt(),
                    typeFlags = reader.readByte().toInt(),
                    typeStrOffset = reader.readUInt(),
                    nameStrOffset = reader.readUInt(),
                    byteSize = reader.readInt(),
                    index = reader.readInt(),
                    metaFlag = reader.readInt()
                )
            )
            if (hVersion >= FormatVersion.TypeTreeNodeWithTypeFlags) {
                reader += 8     //refTypeHash: ULong
            }
        }
        val stringBuffer = reader.read(stringBufferSize)
        fun EndianBinaryReader.readNodeString(value: UInt): String {
            if ((value and 0x80000000u) == 0u) {
                position = value.toLong()
                return readStringUntilNull()
            }
            val offset = value and 0x7FFFFFFFu
            return commonString[offset] ?: offset.toString()
        }
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
        val levelStack = mutableListOf(mutableListOf(0, 1))
        while (levelStack.isNotEmpty()) {
            val (level, count) = levelStack.last()
            if (count == 1) levelStack.removeLast()
            else levelStack.last()[1] -= 1
            val type = reader.readStringUntilNull()
            val name = reader.readStringUntilNull()
            val byteSize = reader.readInt()
            if (hVersion == FormatVersion.Unknown_2) reader += 4   //variableCount
            val index = if (hVersion != FormatVersion.Unknown_3) reader.readInt() else 0
            val typeFlags = reader.readInt()
            val version = reader.readInt()
            val metaFlag = if (hVersion != FormatVersion.Unknown_3) reader.readInt() else 0
            newTree.nodes.add(SerializedType.TreeNode(
                type, name, byteSize, index, typeFlags, version, metaFlag, level
            ))
            val childrenCount = reader.readInt()
            if (childrenCount > 0) levelStack.add(mutableListOf(level + 1, childrenCount))
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