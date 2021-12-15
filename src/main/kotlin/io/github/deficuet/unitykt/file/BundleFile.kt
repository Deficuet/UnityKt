package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*

class BundleFile(private val reader: EndianBinaryReader): AssetNode() {
    data class Block(
        val compressedSize: UInt,
        val uncompressedSize: UInt,
        val flags: UShort
    )

    data class Node(
        val path: String,
        val offset: Long,
        val size: Long,
        val flag: UInt = 0u
    )

    //Header
    private val hSignature: String = reader.readStringUntilNull()
    private val hVersion: UInt = reader.readUInt()

    private val blocksInfo = mutableListOf<Block>()
    private val directoryInfo = mutableListOf<Node>()

    override val files: Map<String, Any>

    init {
        reader.readStringUntilNull()    //hUnityVersion
        reader.readStringUntilNull()    //hUnityRevision
        val filesReader = when (hSignature) {
            "UnityArchive" -> throw UnsupportedFormatException("Unsupported file type UnityArchive")
            "UnityWeb", "UnityRaw" -> {
                if (hVersion == 6u) readFS()
                readWebRaw()
            }
            "UnityFS" -> readFS()
            else -> throw UnsupportedFormatException("Unknown Bundle Signature")
        }
        val fileMap = mutableMapOf<String, Any>()
        for (node in directoryInfo) {
            filesReader.position = node.offset
            val nodeReader = EndianByteArrayReader(
                filesReader.read(node.size.toInt()),
                manualOffset = filesReader.baseOffset + node.offset
            )
            val (nodeType, _) = ImportUtils.checkFileType(nodeReader, OffsetMode.MANUAL)
            var nodeFile: AssetNode? = null
            when (nodeType) {
                FileType.BUNDLE -> nodeFile = BundleFile(nodeReader)
                FileType.WEB -> TODO()
                FileType.ASSETS -> TODO()
                else -> {  }
            }
            fileMap[node.path] = nodeFile ?: nodeReader
        }
        files = fileMap
    }

    private fun readWebRaw(): EndianBinaryReader {  //Web Raw
        val isCompressed = hSignature == "UnityWeb"
        if (hVersion >= 4u) {
            reader.read(16)   //hash
            reader.readUInt()     //crc
        }
        reader.readUInt()   //minStreamedByte
        val hSize = reader.readUInt().toLong()
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
                    Node(
                        readStringUntilNull(),
                        readUInt().toLong(),
                        readUInt().toLong()
                    )
                }
            )
        }
        return blocksReader
    }

    private fun readFS(): EndianBinaryReader {  //FS
        reader.readLong()   //header.size
        val hCompressedBlockSize = reader.readUInt()
        reader.readUInt()   //UncompressedBlockSize
        val hFlags = reader.readUInt()
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
                    Node(
                        offset = readLong(),
                        size = readLong(),
                        flag = readUInt(),
                        path = readStringUntilNull()
                    )
                )
            }
        }
        return EndianByteArrayReader(
            manualOffset = blocksInfoReader.position
        ) {
            blocksInfo.map { block ->
                when (block.flags and 0x3Fu) {
                    1.toUShort() -> CompressUtils.lzmaDecompress(
                        reader.read(block.compressedSize.toInt())
                    )
                    2.toUShort(), 3.toUShort() -> CompressUtils.lz4Decompress(
                        reader.read(block.compressedSize.toInt())
                    )
                    else -> reader.read(block.uncompressedSize.toInt())
                }
            }.sum()
        }
    }
}