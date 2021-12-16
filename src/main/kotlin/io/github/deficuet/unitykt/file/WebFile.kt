package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.*

class WebFile(preReader: EndianBinaryReader): AssetNode() {
    override val files: Map<String, AssetNodeOrReader>
        get() = TODO("Not yet implemented")

    private val reader: EndianBinaryReader

    init {
        var magic = preReader.runThenReset { read(2) }
        reader = if (magic.contentEquals(CompressUtils.GZIP_MAGIC)) {
            EndianByteArrayReader(
                CompressUtils.gzipDecompress(preReader.bytes),
                endian = EndianType.LittleEndian
            )
        } else {
            preReader.position = 0x20
            magic = preReader.runThenReset { read(6) }
            if (magic.contentEquals(CompressUtils.BROTLI_MAGIC)) {
                EndianByteArrayReader(preReader.bytes)
            } else {
                preReader.resetEndian(EndianType.LittleEndian)
            }
        }
        preReader.resetEndian(EndianType.LittleEndian)
        preReader.close()
    }
}