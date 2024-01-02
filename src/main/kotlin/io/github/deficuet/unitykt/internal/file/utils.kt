package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.util.CompressUtils
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.runThenReset

internal fun readerFileType(reader: EndianBinaryReader): FileType {
    if (reader.length < 20) return FileType.RESOURCE
    when (reader.runThenReset { readNullString(20) }) {
        "UnityWeb", "UnityRaw", "UnityArchive", "UnityFS" -> return FileType.BUNDLE
        "UnityWebData1.0" -> return FileType.WEB
        else -> {
            var magic = reader.runThenReset { read(2) }
            if (CompressUtils.GZIP_MAGIC.contentEquals(magic)) {
                return FileType.GZIP
            }
            magic = with(reader) {
                position = 0x20
                runThenReset { read(6) }
            }
            if (CompressUtils.BROTLI_MAGIC.contentEquals(magic)) {
                return FileType.BROTLI
            }
            if (isSerializedFile(reader)) return FileType.ASSETS
            return FileType.RESOURCE
        }
    }
}

internal fun isSerializedFile(reader: EndianBinaryReader) = reader.runThenReset {
    skip(4)    //m_MetadataSize: UInt
    var mFileSize = readUInt32().toLong()
    val mVersion = readUInt32()
    var mDataOffset = readUInt32().toLong()
    skip(4)    //m_Endian(1), m_Reserved(3)
    if (mVersion > 22u) {
        if (length < 48) {
            return@runThenReset false
        }
        skip(4)    //m_MetadataSize: UInt
        mFileSize = readInt64()
        mDataOffset = readInt64()
    }
    return@runThenReset mFileSize == length && mDataOffset <= length
}
