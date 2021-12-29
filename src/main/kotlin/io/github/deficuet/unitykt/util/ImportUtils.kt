package io.github.deficuet.unitykt.util



internal data class FileTypeWithReader(
    val type: FileType, val reader: EndianBinaryReader
)

internal class ImportUtils private constructor() {
    companion object {
        fun checkFileType(input: Any, offsetMode: OffsetMode): FileTypeWithReader {
            val reader = EndianBinaryReader.assign(input, offsetMode)
            if (reader.length < 20) return FileTypeWithReader(FileType.RESOURCE, reader)
            when (reader.runThenReset { readStringUntilNull(20) }) {
                "UnityWeb", "UnityRaw", "UnityArchive", "UnityFS" ->
                    return FileTypeWithReader(FileType.BUNDLE, reader)
                "UnityWebData1.0" -> return FileTypeWithReader(FileType.WEB, reader)
                else -> {
                    var magic = reader.runThenReset { read(2) }
                    if (CompressUtils.GZIP_MAGIC.contentEquals(magic)) {
                        return FileTypeWithReader(FileType.WEB, reader)
                    }
                    magic = with(reader) {
                        position = 0x20
                        runThenReset { read(6) }
                    }
                    if (CompressUtils.BROTLI_MAGIC.contentEquals(magic)) {
                        return FileTypeWithReader(FileType.WEB, reader)
                    }
                    return FileTypeWithReader(
                        if (reader.isSerializedFile()) FileType.ASSETS else FileType.RESOURCE,
                        reader
                    )
                }
            }
        }
    }
}