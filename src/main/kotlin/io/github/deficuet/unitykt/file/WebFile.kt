package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.CompressUtils
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayReader
import java.nio.ByteOrder

class WebFile(
    preReader: EndianBinaryReader,
    override val bundleParent: AssetBundleFile,
    override val name: String
): AssetBundleFile() {
    override val files: Map<String, RawAssetFile>

    private val reader: EndianBinaryReader
    private val directoryInfo = mutableListOf<DirectoryInfoNode>()

    init {
        var magic = preReader.runThenReset { read(2) }
        reader = if (magic.contentEquals(CompressUtils.GZIP_MAGIC)) {
            EndianByteArrayReader(
                CompressUtils.gzipDecompress(preReader.bytes),
                endian = ByteOrder.LITTLE_ENDIAN
            )
        } else {
            preReader.position = 0x20
            magic = preReader.runThenReset { read(6) }
            if (magic.contentEquals(CompressUtils.BROTLI_MAGIC)) {
                EndianByteArrayReader(
                    CompressUtils.brotliDecompress(preReader.bytes),
                    endian = ByteOrder.LITTLE_ENDIAN
                )
            } else {
                preReader.resetEndian(ByteOrder.LITTLE_ENDIAN)
            }
        }
        if (reader.readStringUntilNull() == "UnityWebData1.0") {
            val headLength = reader.readInt()
            while (reader.position < headLength) {
                directoryInfo.add(
                    DirectoryInfoNode(
                        offset = reader.readInt().toLong(),
                        size = reader.readInt().toLong(),
                        path = reader.readString(reader.readInt())
                    )
                )
            }
        }
        files = readFiles(reader, directoryInfo)
        preReader.close()
        reader.close()
    }
}