package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*

//@Suppress("EnumEntryName")
//private enum class FormatVersion(val id: Int) {
//    kUnsupported(1),
//    kUnknown_2(2),
//    kUnknown_3(3),
//    kUnknown_5(5),
//    kUnknown_6(6),
//    kUnknown_7(7),
//    kUnknown_8(8),
//    kUnknown_9(9),
//    kUnknown_10(10),
//    kHasScriptTypeIndex(11),
//    kUnknown_12(12),
//    kHasTypeTreeHashes(13),
//    kUnknown_14(14),
//    kSupportsStrippedObject(15),
//    kRefactoredClassId(16),
//    kRefactorTypeData(17),
//    kRefactorShareableTypeTreeData(18),
//    kTypeTreeNodeWithTypeFlags(19),
//    kSupportsRefObject(20),
//    kStoresTypeDependencies(21),
//    kLargeFilesSupport(22)
//}

private class FormatVersion private constructor() {
    companion object {
        const val kUnsupported = 1u
        const val kUnknown_2 = 2u
        const val kUnknown_3 = 3u
        const val kUnknown_5 = 5u
        const val kUnknown_6 = 6u
        const val kUnknown_7 = 7u
        const val kUnknown_8 = 8u
        const val kUnknown_9 = 9u
        const val kUnknown_10 = 10u
        const val kHasScriptTypeIndex = 11u
        const val kUnknown_12 = 12u
        const val kHasTypeTreeHashes = 13u
        const val kUnknown_14 = 14u
        const val kSupportsStrippedObject = 15u
        const val kRefactoredClassId = 16u
        const val kRefactorTypeData = 17u
        const val kRefactorShareableTypeTreeData = 18u
        const val kTypeTreeNodeWithTypeFlags = 19u
        const val kSupportsRefObject = 20u
        const val kStoresTypeDependencies = 21u
        const val kLargeFilesSupport = 22u
    }
}

class SerializedFile(private val reader: EndianBinaryReader): AssetNode() {
    data class Header(override val map: MutableMap<String, Any> = mutableMapOf()): LateInitDataClass() {
        val metadataSize: UInt      by map
        val fileSize: Long          by map
        val version: UInt           by map
        val dataOffset: Long        by map
        val endian: Byte            by map
        val reserved: ByteArray     by map
    }

    private val header = Header()

    override val files: Map<String, Any>
        get() = TODO("Not yet implemented")

    init {
        with(header) {
            this[::metadataSize] = reader.readUInt()
            this[::fileSize] = reader.readUInt().toLong()
            this[::version] = reader.readUInt()
            this[::dataOffset] = reader.readUInt().toLong()
            if (version >= FormatVersion.kUnknown_9) {
                this[::endian] = reader.readByte()
                this[::reserved] = reader.read(3)
            } else {
                reader.position = fileSize - metadataSize.toLong()
                this[::endian] = reader.readByte()
            }
        }
    }
}