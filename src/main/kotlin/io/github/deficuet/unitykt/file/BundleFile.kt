package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*

internal class ArchiveFlags {
    companion object {
        const val CompressionTypeMask = 0x3Fu
        const val BlocksAndDirectoryInfoCombined = 0x40u
        const val BlocksInfoAtTheEnd = 0x80u
        const val OldWebPluginCompatibility = 0x100u
        const val BlockInfoNeedPaddingAtStart = 0x200u
    }
}

class BundleFile(
    private val reader: EndianBinaryReader,
    override val bundleParent: AssetBundleFile,
    override val name: String
): AssetBundleFile() {
    data class Block(
        val compressedSize: UInt,
        val uncompressedSize: UInt,
        val flags: UShort
    )

    //Header
    private val hSignature: String = reader.readStringUntilNull()
    private val hVersion: UInt = reader.readUInt()

    private val blocksInfo = mutableListOf<Block>()
    private val directoryInfo = mutableListOf<DirectoryInfoNode>()

    override val files: Map<String, RawAssetFile>

    init {
        reader.readStringUntilNull()    //hUnityVersion
        reader.readStringUntilNull()    //hUnityRevision
        val filesReader = when (hSignature) {
            "UnityArchive" -> throw UnsupportedFormatException("Unsupported file type UnityArchive")
            "UnityWeb", "UnityRaw" -> {
                if (hVersion == 6u) readFS()
                else readWebRaw()
            }
            "UnityFS" -> readFS()
            else -> throw UnsupportedFormatException("Unknown Bundle Signature")
        }
        files = readFiles(filesReader, directoryInfo)
        filesReader.close()
        reader.close()
    }

    private fun readWebRaw(): EndianBinaryReader {
        val isCompressed = hSignature == "UnityWeb"
        if (hVersion >= 4u) {
            reader += 20   //hash(16), crc: UInt
        }
        reader += 4   //minStreamedByte: UInt
        val hSize = reader.readUInt().toLong()
        reader += 4   //levelsBeforeStreaming: UInt
        val levelCount = reader.readInt()
        reader += 4 * 2 * (levelCount - 1)
        blocksInfo.add(
            Block(
                reader.readUInt(), reader.readUInt(), 0u
            )
        )
        if (hVersion >= 2u) {
            reader += 4   //completeFileSize: UInt
        }
        if (hVersion >= 3u) {
            reader += 4   //fileInfoHeaderSize: UInt
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
        val nodesCount = blocksReader.readInt()
        for (i in 0 until nodesCount) {
            directoryInfo.add(
                with(blocksReader) {
                    DirectoryInfoNode(
                        readStringUntilNull(),
                        readUInt().toLong(),
                        readUInt().toLong()
                    )
                }
            )
        }
        return blocksReader
    }

    private fun readFS(): EndianBinaryReader {
        reader += 8   //header.size: Long
        val hCompressedBlockSize = reader.readUInt()
        val uncompressedBlockSize = reader.readUInt()
        val hFlags = reader.readUInt()
        if (hSignature != "UnityFS") reader += 1
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
        blocksInfoReader += 16   //uncompressedDataHash
        val blocksInfoCount = blocksInfoReader.readInt()
        for (i in 0 until blocksInfoCount) {
            blocksInfo.add(
                Block(
                    uncompressedSize = blocksInfoReader.readUInt(),
                    compressedSize = blocksInfoReader.readUInt(),
                    flags = blocksInfoReader.readUShort()
                )
            )
        }
        val nodesCount = blocksInfoReader.readInt()
        with(blocksInfoReader) {
            for (j in 0 until nodesCount) {
                directoryInfo.add(
                    DirectoryInfoNode(
                        offset = readLong(),
                        size = readLong(),
                        flag = readUInt(),
                        path = readStringUntilNull()
                    )
                )
            }
        }
        if (hFlags.and(ArchiveFlags.BlockInfoNeedPaddingAtStart) != 0u) {
            reader.alignStream(16)
        }
        return EndianByteArrayReader(
            manualOffset = blocksInfoReader.realOffset
        ) {
            blocksInfo.map { block ->
                when (block.flags.and(ArchiveFlags.CompressionTypeMask.toUShort())) {
                    1.toUShort() -> CompressUtils.lzmaDecompress(
                        reader.read(block.compressedSize.toInt())
                    )
                    2.toUShort(), 3.toUShort() -> CompressUtils.lz4Decompress(
                        reader.read(block.compressedSize.toInt()), block.uncompressedSize.toInt()
                    )
                    else -> reader.read(block.uncompressedSize.toInt())
                }
            }.sum()
        }
    }
}