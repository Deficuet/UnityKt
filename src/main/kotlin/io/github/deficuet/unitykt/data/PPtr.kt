package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*

class PPtr<T: Object> internal constructor(reader: ObjectReader) {
    var mFileID = reader.readInt()
        internal set
    var mPathID = with(reader) { if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong() }
        internal set
    val isNull = mPathID == 0L || mFileID < 0
    val assetFile = reader.assetFile

    @PublishedApi internal var obj: T? = null
}

internal inline fun <reified O: Object> PPtr<O>.setObjInfo(impl: ObjectImpl) {
    val name = impl.assetFile.name
    if (name.contentEquals(assetFile.name)) {
        mFileID = 0
    } else {
        mFileID = assetFile.externals.indexOfFirst { it.name.contentEquals(name) }
        if (mFileID == -1) {
            (assetFile.externals as MutableList).add(
                SerializedFile.FileIdentifier(
                    kotlin.byteArrayOf(), 0, impl.assetFile.name
                )
            )
            mFileID = assetFile.externals.size
        } else {
            mFileID += 1
        }
    }
    mPathID = impl.mPathID
    obj = null
}