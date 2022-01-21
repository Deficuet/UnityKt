package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.AssetManager
import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.dataImpl.ObjectImpl
import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import java.io.File

class PPtr<T: Object> internal constructor(reader: ObjectReader) {
    var mFileID = reader.readInt()
        internal set
    var mPathID = with(reader) { if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong() }
        internal set
    val isNull = mPathID == 0L || mFileID < 0
    val assetFile = reader.assetFile

    @PublishedApi internal var obj: T? = null

    @PublishedApi internal fun getManager(): SerializedFile? {
        return if (mFileID == 0) {
            assetFile
        } else if (mFileID > 0 && mFileID - 1 < assetFile.externals.size) {
            val parent = assetFile.bundleParent
            val name = assetFile.externals[mFileID - 1].name
            if (parent !is ImportContext) {
                (parent.files.tryGetOrUppercase(name) as? SerializedFile).let {
                    if (it == null) {
                        val path = parent.bundleParent.root.directory
                        if (path.isNotEmpty()) {
                            val actualName = StringRef()
                            if (path.listFiles().containsIgnoreCase(name, actualName)) {
                                val new = AssetManager.loadFile("$path/${actualName.value}")
                                new.files.getValue(actualName.value) as SerializedFile
                            } else null
                        } else null
                    } else it
                }
            } else {
                AssetManager.assetFiles.tryGetOrUppercase(name).let {
                    if (it == null) {
                        val new = if (File("${parent.directory}/$name").exists()) {
                            AssetManager.loadFile("${parent.directory}/$name")
                        } else if (File("${parent.directory}/${name.uppercase()}").exists()) {
                            AssetManager.loadFile("${parent.directory}/${name.uppercase()}")
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