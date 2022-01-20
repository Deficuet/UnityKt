package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.dataImpl.PPtr
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.StringRef
import java.io.File

inline fun <reified O: Object> PPtr<O>.getObj(): O? {
    if (obj != null) return obj
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
        return if (objFound is O) {
            obj = objFound
            obj
        } else {
            null
        }
    }
    return null
}

