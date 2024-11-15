package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.internal.utils.CompressUtils
import io.github.deficuet.unitykt.utils.UnityVersion
import io.github.deficuet.unitykt.utils.EndianBinaryReader
import io.github.deficuet.unitykt.utils.EndianByteArrayReader
import io.github.deficuet.unitykt.utils.readArrayOf
import io.github.deficuet.unitykt.utils.withMark

internal class ArchiveFlags private constructor() {
    companion object {
        const val COMPRESSION_TYPE_MASK = 0x3Fu
        const val BLOCKS_AND_DIRECTORY_INFO_COMBINED = 0x40u
        const val BLOCKS_INFO_AT_THE_END = 0x80u
//        const val OldWebPluginCompatibility = 0x100u
        const val BLOCK_INFO_NEED_PADDING_AT_START = 0x200u
    }
}

internal class CNEncryptionFlags private constructor() {
    companion object {
        const val OLD_FLAG = 0x200u
        const val NEW_FLAG = 0x400u
    }
}

internal class BundleFile(
    private val reader: EndianBinaryReader,
    override val parent: FileNode,
    override val name: String
): AbstractFile {
    data class Header(
        val signature: String,
        val version: UInt,
        val unityVersion: String,
        val unityRevision: UnityVersion,
    )

    data class StorageBlock(
        val compressedSize: UInt,
        val uncompressedSize: UInt,
        val flags: UShort = 0u
    )

    private val header = Header(
        reader.readNullString(),
        reader.readUInt32(),
        reader.readNullString(),
        UnityVersion(reader.readNullString())
    )

    private val storageBlocks = mutableListOf<StorageBlock>()
    private val directoryInfoNodes = mutableListOf<DirectoryInfoNode>()

    init {
        val filesReader = when (header.signature) {
            "UnityArchive" -> throw UnsupportedOperationException("Unsupported file type UnityArchive")
            "UnityWeb", "UnityRaw" -> {
                if (header.version == 6u) readFS()
                else readWebRaw()
            }
            "UnityFS" -> readFS()
            else -> throw UnsupportedOperationException("Unknown Bundle Signature")
        }
        readFiles(filesReader, directoryInfoNodes)
        filesReader.close()
        reader.close()
    }

    private fun readWebRaw(): EndianBinaryReader {
        if (header.version >= 4u) {
            reader.skip(20)   //hash(16), crc: UInt
        }
        reader.skip(4)   //minStreamedByte: UInt
        val size = reader.readUInt32().toLong()
        reader.skip(4)   //levelsBeforeStreaming: UInt
        val blocks = reader.readArrayOf {
            StorageBlock(
                compressedSize = readUInt32(),
                uncompressedSize = readUInt32()
            )
        }
        storageBlocks.add(blocks.last())
        if (header.version >= 2u) {
            reader.skip(4)   //completeFileSize: UInt
        }
        if (header.version >= 3u) {
            reader.skip(4)   //fileInfoHeaderSize: UInt
        }
        reader.position = size
        val isCompressed = header.signature == "UnityWeb"
        val blocksReader = EndianByteArrayReader(
            baseOffset = size
        ) {
            var sum = 0
            val result = ByteArray(storageBlocks.sumOf { it.uncompressedSize.toInt() })
            for (block in storageBlocks) {
                val compressed = reader.read(block.compressedSize.toInt())
                val decompressed = if (isCompressed) {
                    CompressUtils.lzmaDecompress(compressed)
                } else compressed
                System.arraycopy(
                    decompressed, 0,
                    result, sum, decompressed.size
                )
                sum += decompressed.size
            }
            result
        }
        directoryInfoNodes.addAll(
            blocksReader.readArrayOf {
                DirectoryInfoNode(
                    path = readNullString(),
                    offset = readUInt32().toLong(),
                    size = readUInt32().toLong()
                )
            }
        )
        return blocksReader
    }

    private fun readFS(): EndianBinaryReader {
        val size = reader.readInt64()
        val compressedBlockSize = reader.readUInt32()
        val uncompressedBlockSize = reader.readUInt32()
        val flags = reader.readUInt32()
        if (header.signature != "UnityFS") reader.skip(1)
        val isCNEncrypted = header.unityRevision.let { ur ->
            if (
                ur < 2020 ||
                (ur >= 2020 && ur < intArrayOf(2020, 3, 34)) ||
                (ur >= 2021 && ur < intArrayOf(2021, 3, 2)) ||
                (ur >= 2022 && ur < intArrayOf(2022, 1, 1))
            ) {
                flags.and(CNEncryptionFlags.OLD_FLAG) != 0u
            } else {
                flags.and(CNEncryptionFlags.NEW_FLAG) != 0u
            }
        }
        if (isCNEncrypted) {
            throw UnsupportedOperationException("UnityCN encryption detected")
        }
        if (header.version >= 7u) {
            reader.alignStream(16)
        } else if (
            header.unityRevision >= intArrayOf(2019, 4) &&
            flags != ArchiveFlags.BLOCKS_AND_DIRECTORY_INFO_COMBINED
        ) {
            val preAlign = reader.position
            val alignData = reader.read((16 - (preAlign % 16).toInt()) % 16)
            if (alignData.any { it.toInt() != 0 }) {
                reader.position = preAlign
            }
        }
        val blockOffset = reader.position
        var blocksInfoBytes = if (flags.and(ArchiveFlags.BLOCKS_INFO_AT_THE_END) != 0u) {
            reader.withMark {
                position = size - compressedBlockSize.toLong()
                read(compressedBlockSize.toInt())
            }
        } else {
            reader.read(compressedBlockSize.toInt())
        }
        blocksInfoBytes = when (val ct = flags.and(ArchiveFlags.COMPRESSION_TYPE_MASK)) {
            1u -> CompressUtils.lzmaDecompress(blocksInfoBytes)
            2u, 3u -> CompressUtils.lz4Decompress(blocksInfoBytes, uncompressedBlockSize.toInt())
            else -> throw UnsupportedOperationException("Unknown compression type $ct")
        }
        return EndianByteArrayReader(blocksInfoBytes, baseOffset = blockOffset).use { blocksInfoReader ->
            blocksInfoReader.skip(16)   //uncompressedDataHash
            storageBlocks.addAll(
                blocksInfoReader.readArrayOf {
                    StorageBlock(
                        uncompressedSize = readUInt32(),
                        compressedSize = readUInt32(),
                        flags = readUInt16()
                    )
                }
            )
            directoryInfoNodes.addAll(
                blocksInfoReader.readArrayOf {
                    DirectoryInfoNode(
                        offset = readInt64(),
                        size = readInt64(),
                        flag = readUInt32(),
                        path = readNullString()
                    )
                }
            )
            if (flags.and(ArchiveFlags.BLOCK_INFO_NEED_PADDING_AT_START) != 0u) {
                reader.alignStream(16)
            }
            EndianByteArrayReader(
                baseOffset = blocksInfoReader.realOffset
            ) {
                var sum = 0
                val result = ByteArray(storageBlocks.sumOf { it.uncompressedSize }.toInt())
                for (block in storageBlocks) {
                    val decompressed = when (val ct = block.flags.toUInt().and(ArchiveFlags.COMPRESSION_TYPE_MASK)) {
                        1u -> CompressUtils.lzmaDecompress(
                            reader.read(block.compressedSize.toInt())
                        )
                        2u, 3u -> CompressUtils.lz4Decompress(
                            reader.read(block.compressedSize.toInt()), block.uncompressedSize.toInt()
                        )
                        else -> throw UnsupportedOperationException("Unknown compression type $ct")
                    }
                    System.arraycopy(
                        decompressed, 0,
                        result, sum, decompressed.size
                    )
                    sum += decompressed.size
                }
                result
            }
        }
    }
}
