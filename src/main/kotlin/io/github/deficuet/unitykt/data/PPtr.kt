package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.AssetManager
import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import java.io.File

class PPtr<T: Object> internal constructor(reader: ObjectReader) {
    val mFileID = reader.readInt()
    val mPathID = with(reader) {
        if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong()
    }
    val isNull = mPathID == 0L || mFileID < 0
    private val assetFile = reader.assetFile
    val obj: T? = null
        get() {
            if (field != null) return field
            val manager: SerializedFile?
            if (mFileID == 0) manager = assetFile
            else if (mFileID > 0 && mFileID - 1 < assetFile.externals.size) {
                val parent = assetFile.bundleParent
                val name = assetFile.externals[mFileID - 1].name
                if (parent !is ImportContext) {
                    manager = (parent.files.tryGetOrUppercase(name) as? SerializedFile).let {
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
                    manager = AssetManager.assetFiles.tryGetOrUppercase(name).let {
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
            } else { manager = null }
            if (manager != null && mPathID in manager.objectDict) {
                @Suppress("UNCHECKED_CAST")
                return try { manager.objectDict.getValue(mPathID) as T } catch (e: Exception) { null }
            }
            return null
        }
}