package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.*

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

inline fun <reified T: Object> Array<out PPtr<*>>.getAllInstanceOf(): List<T> {
    return map { it.getObj() }.filterIsInstance<T>()
}

inline fun <reified T: Object> List<PPtr<*>>.getAllInstanceOf(): List<T> {
    return map { it.getObj() }.filterIsInstance<T>()
}

inline fun <reified T: Object> Array<out PPtr<*>>.firstObjectOf(): T {
    return mapNotNull { it.getObj() }.firstObjectOf()
}

inline fun <reified T: Object> List<PPtr<*>>.firstObjectOf(): T {
    return mapNotNull { it.getObj() }.firstObjectOf()
}