package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import java.io.File

class PPtr<out T: Object> internal constructor(reader: ObjectReader) {
    var mFileID = reader.readInt()
        internal set
    var mPathID = with(reader) { if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong() }
        internal set
    val isNull = mPathID == 0L || mFileID < 0
    val assetFile = reader.assetFile

    /**
     * @see io.github.deficuet.unitykt.getObj
     */
    @PublishedApi internal var obj: @UnsafeVariance T? = null

    @PublishedApi internal fun getManager(): SerializedFile? {
        return if (mFileID == 0) {
            assetFile
        } else if (mFileID > 0 && mFileID - 1 < assetFile.externals.size) {
            val parent = assetFile.bundleParent
            val manager = assetFile.root.manager
            val name = assetFile.externals[mFileID - 1].name
            if (parent !is ImportContext) {
                (parent.files.tryGetOrUppercase(name) as? SerializedFile).let {
                    if (it == null) {
                        val path = parent.bundleParent.root.directory
                        if (path.isNotEmpty()) {
                            val actualName = StringRef()
                            if (path.listFiles().containsIgnoreCase(name, actualName)) {
                                val new = ImportContext("$path/${actualName.value}", manager)
                                new.files.getValue(actualName.value) as SerializedFile
                            } else null
                        } else null
                    } else it
                }
            } else {
                manager.assetFiles.tryGetOrUppercase(name).let {
                    if (it == null) {
                        val new = if (File("${parent.directory}/$name").exists()) {
                            ImportContext("${parent.directory}/$name", manager)
                        } else if (File("${parent.directory}/${name.uppercase()}").exists()) {
                            ImportContext("${parent.directory}/${name.uppercase()}", manager)
                        } else null
                        new?.files?.tryGetOrUppercase(name)!!.let { externalFile ->
                            if (externalFile is SerializedFile) {
                                externalFile
                            } else null
                        }
                    } else it
                }
            }
        } else { null }
    }

    internal fun setObjInfo(impl: ObjectImpl) {
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
}