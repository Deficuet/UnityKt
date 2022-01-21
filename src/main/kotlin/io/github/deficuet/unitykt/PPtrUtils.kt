package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import java.io.File

inline fun <reified O: Object> PPtr<O>.getObj(): O? {
    if (obj != null) return obj
    getManager()?.let { manager ->
        if (mPathID in manager.objectDict) {
            val objFound = manager.objectDict.getValue(mPathID)
            return if (objFound is O) {
                obj = objFound
                obj
            } else {
                null
            }
        }
    }
    return null
}

