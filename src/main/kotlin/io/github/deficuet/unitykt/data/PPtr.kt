package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.util.ObjectReader

class PPtr<T: Object>(private val reader: ObjectReader) {
    val mFileID = reader.readInt()
    val mPathID = with(reader) {
        if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong()
    }
    private val assetFile = reader.assetFile
    val serializedFile by lazy {
        if (mFileID == 0) assetFile
        if (mFileID > 0 && mFileID - 1 < assetFile.externals.size) {

        }
    }
}