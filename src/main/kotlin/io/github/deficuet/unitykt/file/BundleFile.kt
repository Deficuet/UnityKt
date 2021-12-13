package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*

class BundleFile(private val reader: EndianBinaryReader): AssetBundle() {
    data class Header(
        override val map: MutableMap<String, Any> = mutableMapOf()
    ): LateInitDataClass() {
        val signature: String               by map
        val version: UInt                   by map
        val unityVersion: String            by map
        val unityRevision: String           by map
        val size: Long                      by map
        val compressedBlockSize: UInt       by map
        val uncompressedBlockSize: UInt     by map
        val flags: UInt                     by map
    }

    data class Block(
        val compressedSize: UInt,
        val uncompressedSize: UInt,
        val flags: UShort
    )

    data class Node(
        val path: String,
        val offset: UInt,
        val size: UInt
    )

    private fun fsBranch() {

    }

    private val header = Header()
    private val blocksInfo = mutableListOf<Block>()
    private val directoryInfo = mutableListOf<Node>()

    init {
        with(header) {
            this[::signature] = reader.readStringUntilNull()
            this[::version] = reader.readUInt()
            this[::unityVersion] = reader.readStringUntilNull()
            this[::unityRevision] = reader.readStringUntilNull()
        }
        when (header.signature) {
            "UnityArchive" -> throw UnsupportedFormatException("Unsupported file type UnityArchive")
            "UnityWeb", "UnityRaw" -> {
                if (header.version == 6u) {
                    fsBranch()
                }
            }
            "UnityFs" -> {

            }
        }
    }

    private fun readHeaderAndBlockInfo() {  //Web Raw
        val isCompressed = header.signature == "UnityWeb"
        if (header.version >= 4u) {
            reader.read(16)   //hash
            reader.readUInt()     //crc
        }
        reader.readUInt()   //minStreamedByte
        header[header::size] = reader.readUInt()
        reader.readUInt()   //levelsBeforeStreaming
        val levelCount = reader.readInt()
        reader.plusAbsPos(4 * 2 * (levelCount - 1))
        blocksInfo.add(
            Block(
                reader.readUInt(), reader.readUInt(),
                (if (isCompressed) 1 else 0).toUShort()
            )
        )
        if (header.version >= 2u) {
            reader.readUInt()   //completeFileSize
        }
        if (header.version >= 3u) {
            reader.readUInt()   //fileInfoHeaderSize
        }
        reader.position = header.size
        val uncompressedBytes = with(reader.read(blocksInfo[0].compressedSize.toInt())) {
            if (blocksInfo[0].flags == 1.toUShort()) {
                CompressUtils.lzmaDecompress(this)
            } else this
        }
        val blocksReader = EndianByteArrayReader(
            uncompressedBytes,
            manualOffset = header.size,
            offsetMode = OffsetMode.MANUAL
        )
        val nodesCount = blocksReader.readInt()
        for (i in 0 until nodesCount) {
            directoryInfo.add(
                with(blocksReader) {
                    Node(
                        readStringUntilNull(),
                        readUInt(), readUInt()
                    )
                }
            )
        }
    }

    private fun readHeader() {  //FS
        with(header) {
            this[::size] = reader.readLong()
            this[::compressedBlockSize] = reader.readUInt()
            this[::uncompressedBlockSize] = reader.readUInt()
            this[::flags] = reader.readUInt()
            if (signature != "UnityFS") reader.read(1)
            if (version >= 7u) reader.alignStream(16)
        }

    }
}