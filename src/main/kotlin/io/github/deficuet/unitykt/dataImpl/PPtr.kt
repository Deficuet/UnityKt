package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.AssetManager
import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.file.FormatVersion
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import java.io.File
import kotlin.reflect.KClass

class PPtr<T: ObjectImpl> private constructor(reader: ObjectReader, private val clazz: KClass<T>) {
    var mFileID = reader.readInt()
        private set
    var mPathID = with(reader) { if (formatVersion < FormatVersion.kUnknown_14) readInt().toLong() else readLong() }
        private set
    val isNull = mPathID == 0L || mFileID < 0
    private val assetFile = reader.assetFile
    var obj: T? = null
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
                val objFound = manager.objectDict.getValue(mPathID)
                return if (clazz.isInstance(objFound)) {
                    @Suppress("UNCHECKED_CAST")
                    field = objFound as T
                    field
                } else {
                    null
                }
            }
            return null
        }
    internal set(value) {
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
    }

    companion object {
        internal inline operator fun <reified O: ObjectImpl> invoke(reader: ObjectReader): PPtr<O> {
            return PPtr(reader, O::class)
        }
    }
}