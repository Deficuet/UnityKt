package io.github.deficuet.unitykt.util

internal enum class FileType {
    ASSETS, BUNDLE, WEB, RESOURCE, GZIP, BROTLI
}

internal data class FileTypeWithReader(
    val type: FileType, val reader: EndianBinaryReader
)

internal object ImportUtils {
    private val gzipMagic = byteArrayOf(0xBF, 0x8B)
    private val brotliMagic = byteArrayOf(0x62, 0x72, 0x6F, 0x74, 0x6C, 0x69)

    fun checkFileType(input: Any, offsetMode: OffsetMode): FileTypeWithReader {
        val reader = EndianBinaryReader.assign(input, offsetMode)
        if (reader.length < 20) return FileTypeWithReader(FileType.RESOURCE, reader)
        when (reader.runThenReset { readStringUntilNull(20) }) {
            "UnityWeb", "UnityRaw", "UnityArchive", "UnityFS" ->
                return FileTypeWithReader(FileType.BUNDLE, reader)
            "UnityWebData1.0" -> return FileTypeWithReader(FileType.WEB, reader)
            else -> {
                var magic = reader.runThenReset { read(2) }
                if (gzipMagic.contentEquals(magic)) {
                    return FileTypeWithReader(FileType.GZIP, reader)
                }
                magic = with(reader) {
                    position = 0x20
                    runThenReset { read(6) }
                }
                if (brotliMagic.contentEquals(magic)) {
                    return FileTypeWithReader(FileType.BROTLI, reader)
                }
                return if (reader.isSerializedFile())
                    FileTypeWithReader(FileType.ASSETS, reader)
                else FileTypeWithReader(FileType.RESOURCE, reader)
            }
        }
    }
}