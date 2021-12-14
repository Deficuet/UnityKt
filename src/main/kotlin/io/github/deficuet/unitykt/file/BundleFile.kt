package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*
import kotlin.properties.Delegates

class BundleFile(private val reader: EndianBinaryReader): AssetBundle() {
    data class Block(
        val compressedSize: UInt,
        val uncompressedSize: UInt,
        val flags: UShort
    )

    abstract class Node { abstract val path: String }

    data class NodeWeb(
        override val path: String,
        val offset: UInt,
        val size: UInt
    ): Node()

    data class NodeFS(
        val offset: Long,
        val size: Long,
        val flags: UInt,
        override val path: String
    ): Node()

    private fun fsBranch() {

    }

    //Header
    private val hSignature: String = reader.readStringUntilNull()
    private val hVersion: UInt = reader.readUInt()
    private val hUnityVersion: String = reader.readStringUntilNull()
    private val hUnityRevision: String = reader.readStringUntilNull()
    private var hSize by Delegates.notNull<Long>()
    private var hCompressedBlockSize by Delegates.notNull<UInt>()
    private var hUncompressedBlockSize by Delegates.notNull<UInt>()
    private var hFlags by Delegates.notNull<UInt>()

    private val blocksInfo = mutableListOf<Block>()
    private val directoryInfo = mutableListOf<Node>()

    init {
        when (hSignature) {
            "UnityArchive" -> throw UnsupportedFormatException("Unsupported file type UnityArchive")
            "UnityWeb", "UnityRaw" -> {
                if (hVersion == 6u) {
                    fsBranch()
                }
                val blocksReader = readHeaderAndBlockInfo()
            }
            "UnityFs" -> {

            }
        }
    }

    private fun readHeaderAndBlockInfo(): EndianBinaryReader {  //Web Raw
        val isCompressed = hSignature == "UnityWeb"
        if (hVersion >= 4u) {
            reader.read(16)   //hash
            reader.readUInt()     //crc
        }
        reader.readUInt()   //minStreamedByte
        hSize = reader.readUInt().toLong()
        reader.readUInt()   //levelsBeforeStreaming
        val levelCount = reader.readInt()
        reader.plusAbsPos(4 * 2 * (levelCount - 1))
        blocksInfo.add(
            Block(
                reader.readUInt(), reader.readUInt(),
                (if (isCompressed) 1 else 0).toUShort()
            )
        )
        if (hVersion >= 2u) {
            reader.readUInt()   //completeFileSize
        }
        if (hVersion >= 3u) {
            reader.readUInt()   //fileInfoHeaderSize
        }
        reader.position = hSize
        val uncompressedBytes = with(reader.read(blocksInfo[0].compressedSize.toInt())) {
            if (blocksInfo[0].flags == 1.toUShort()) {
                CompressUtils.lzmaDecompress(this)
            } else this
        }
        val blocksReader = EndianByteArrayReader(
            uncompressedBytes,
            manualOffset = hSize
        )
        val nodesCount = blocksReader.readInt()
        for (i in 0 until nodesCount) {
            directoryInfo.add(
                with(blocksReader) {
                    NodeWeb(
                        readStringUntilNull(),
                        readUInt(), readUInt()
                    )
                }
            )
        }
        return blocksReader
    }

    private fun readHeader() {  //FS
        hSize = reader.readLong()
        hCompressedBlockSize = reader.readUInt()
        hUncompressedBlockSize = reader.readUInt()
        hFlags = reader.readUInt()
        if (hSignature != "UnityFS") reader.read(1)
        if (hVersion >= 7u) reader.alignStream(16)
        val blockOffset = reader.position
        var blocksInfoBytes: ByteArray
        if ((hFlags and 0x80u) != 0u) {
            with(reader) {
                mark()
                position = length - hCompressedBlockSize.toLong()
                blocksInfoBytes = read(hCompressedBlockSize.toInt())
                reset()
            }
        } else {
            blocksInfoBytes = reader.read(hCompressedBlockSize.toInt())
        }
        when (hFlags and 0x3Fu) {
            1u -> blocksInfoBytes = CompressUtils.lzmaDecompress(blocksInfoBytes)
            2u, 3u -> blocksInfoBytes = CompressUtils.lz4Decompress(blocksInfoBytes)
        }
        val blocksInfoReader = EndianByteArrayReader(blocksInfoBytes, manualOffset = blockOffset)
        blocksInfoReader.read(16)   //uncompressedDataHash
        val blocksInfoCount = blocksInfoReader.readInt()
        for (i in 0 until blocksInfoCount) {
            blocksInfo.add(
                Block(
                    blocksInfoReader.readUInt(),
                    blocksInfoReader.readUInt(),
                    blocksInfoReader.readUShort()
                )
            )
        }
        val nodesCount = blocksInfoReader.readInt()

    }
}