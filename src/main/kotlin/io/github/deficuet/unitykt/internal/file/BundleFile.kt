package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.util.*

internal class ArchiveFlags private constructor() {
    companion object {
        const val CompressionTypeMask = 0x3Fu
//        const val BlocksAndDirectoryInfoCombined = 0x40u
        const val BlocksInfoAtTheEnd = 0x80u
//        const val OldWebPluginCompatibility = 0x100u
        const val BlockInfoNeedPaddingAtStart = 0x200u
    }
}

internal class BundleFile(
    private val reader: EndianBinaryReader,
    override val bundleParent: FileNode,
    override val name: String
): AssetFile {
    data class Block(
        val compressedSize: UInt,
        val uncompressedSize: UInt,
        val flags: UShort
    )

    //Header
    private val hSignature: String = reader.readNullString()
    private val hVersion: UInt = reader.readUInt32()

    private val blocksInfo = mutableListOf<Block>()
    private val directoryInfo = mutableListOf<DirectoryInfoNode>()

    init {
        reader.readNullString()    //hUnityVersion
        reader.readNullString()    //hUnityRevision
        val filesReader = when (hSignature) {
            "UnityArchive" -> throw UnsupportedOperationException("Unsupported file type UnityArchive")
            "UnityWeb", "UnityRaw" -> {
                if (hVersion == 6u) readFS()
                else readWebRaw()
            }
            "UnityFS" -> readFS()
            else -> throw UnsupportedOperationException("Unknown Bundle Signature")
        }
        readFiles(filesReader, directoryInfo)
        filesReader.close()
        reader.close()
    }

    private fun readWebRaw(): EndianBinaryReader {
        val isCompressed = hSignature == "UnityWeb"
        if (hVersion >= 4u) {
            reader.skip(20)   //hash(16), crc: UInt
        }
        reader.skip(4)   //minStreamedByte: UInt
        val hSize = reader.readUInt32().toLong()
        reader.skip(4)   //levelsBeforeStreaming: UInt
        val levelCount = reader.readInt32()
        reader.skip(4 * 2 * (levelCount - 1))
        blocksInfo.add(
            Block(
                reader.readUInt32(), reader.readUInt32(), 0u
            )
        )
        if (hVersion >= 2u) {
            reader.skip(4)   //completeFileSize: UInt
        }
        if (hVersion >= 3u) {
            reader.skip(4)   //fileInfoHeaderSize: UInt
        }
        reader.position = hSize
        val uncompressedBytes = with(reader.read(blocksInfo[0].compressedSize.toInt())) {
            if (isCompressed) {
                CompressUtils.lzmaDecompress(this)
            } else this
        }
        val blocksReader = EndianByteArrayReader(
            uncompressedBytes,
            baseOffset = hSize
        )
        directoryInfo.addAll(
            blocksReader.readArrayOf {
                DirectoryInfoNode(
                    readNullString(),
                    readUInt32().toLong(),
                    readUInt32().toLong()
                )
            }
        )
        return blocksReader
    }

    private fun readFS(): EndianBinaryReader {
        reader.skip(8)   //header.size: Long
        val hCompressedBlockSize = reader.readUInt32()
        val uncompressedBlockSize = reader.readUInt32()
        val hFlags = reader.readUInt32()
        if (hSignature != "UnityFS") reader.skip(1)
        if (hVersion >= 7u) reader.alignStream(16)
        val blockOffset = reader.position
        var blocksInfoBytes: ByteArray
        if (hFlags.and(ArchiveFlags.BlocksInfoAtTheEnd) != 0u) {
            blocksInfoBytes = reader.withMark {
                position = length - hCompressedBlockSize.toLong()
                read(hCompressedBlockSize.toInt())
            }
        } else {
            blocksInfoBytes = reader.read(hCompressedBlockSize.toInt())
        }
        when (hFlags.and(ArchiveFlags.CompressionTypeMask)) {
            1u -> blocksInfoBytes = CompressUtils.lzmaDecompress(blocksInfoBytes)
            2u, 3u -> blocksInfoBytes = CompressUtils.lz4Decompress(blocksInfoBytes, uncompressedBlockSize.toInt())
        }
        val blocksInfoReader = EndianByteArrayReader(blocksInfoBytes, baseOffset = blockOffset)
        blocksInfoReader.skip(16)   //uncompressedDataHash
        blocksInfo.addAll(
            blocksInfoReader.readArrayOf {
                Block(
                    uncompressedSize = readUInt32(),
                    compressedSize = readUInt32(),
                    flags = readUInt16()
                )
            }
        )
        directoryInfo.addAll(
            blocksInfoReader.readArrayOf {
                DirectoryInfoNode(
                    offset = readInt64(),
                    size = readInt64(),
                    flag = readUInt32(),
                    path = readNullString()
                )
            }
        )
        if (hFlags.and(ArchiveFlags.BlockInfoNeedPaddingAtStart) != 0u) {
            reader.alignStream(16)
        }
        return EndianByteArrayReader(
            baseOffset = blocksInfoReader.realOffset
        ) {
            var sum = 0
            val result = ByteArray(blocksInfo.sumOf { it.uncompressedSize }.toInt())
            for (block in blocksInfo) {
                val decompressed = when (block.flags.toUInt().and(ArchiveFlags.CompressionTypeMask)) {
                    1u -> CompressUtils.lzmaDecompress(
                        reader.read(block.compressedSize.toInt())
                    )
                    2u, 3u -> CompressUtils.lz4Decompress(
                        reader.read(block.compressedSize.toInt()), block.uncompressedSize.toInt()
                    )
                    else -> reader.read(block.uncompressedSize.toInt())
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