package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.AssetManager
import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import io.github.deficuet.unitykt.get

class PPtr<T: Object> internal constructor(reader: ObjectReader) {
    var mFileID = reader.readInt()
        private set
    var mPathID = with(reader) { if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong() }
        private set
    val isNull = mPathID == 0L || mFileID < 0
    val assetFile = reader.assetFile
    var obj: T? = null
        set(value) {
            if (value == null) throw IllegalArgumentException("The value set to PPtr can not be null.")
            val name = value.assetFile.name
            if (name.contentEquals(assetFile.name)) {
                mFileID = 0
            } else {
                mFileID = assetFile.externals.indexOfFirst { it.name.contentEquals(name) }
                if (mFileID == -1) {
                    (assetFile.externals as MutableList).add(
                        SerializedFile.FileIdentifier(
                            kotlin.byteArrayOf(), 0, value.assetFile.name
                        )
                    )
                    mFileID = assetFile.externals.size
                } else {
                    mFileID += 1
                }
            }
            mPathID = value.mPathID
            field = null
        }

    internal inline fun <reified O: T> setObjImplWrapper(o: ObjectImpl) {
        obj = AssetManager.objectDict[o.mPathID] as O
    }
}