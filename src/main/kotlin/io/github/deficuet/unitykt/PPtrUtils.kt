package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.data.PPtr

inline fun <reified O: Object> PPtr<O>.safeGetObj(): O? {
    if (obj != null) return obj
    getManager()?.let { manager ->
        if (mPathID in manager.objects) {
            val objFound = manager.objects.getValue(mPathID)
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

inline fun <reified O: Object> PPtr<O>.getObj(): O {
    return safeGetObj()!!
}

inline fun <reified O: Object> PPtr<*>.safeGetObjAs(): O? {
    return safeGetObj() as? O
}

inline fun <reified T: Object> PPtr<*>.getObjAs(): T {
    return safeGetObj() as T
}

inline fun <reified T: Object> Array<out PPtr<*>>.allObjectsOf(): List<T> {
    return map { it.safeGetObj() }.filterIsInstance<T>()
}

inline fun <reified T: Object> Collection<PPtr<*>>.allObjectsOf(): List<T> {
    return map { it.safeGetObj() }.filterIsInstance<T>()
}

inline fun <reified T: Object> Array<out PPtr<*>>.firstObjectOf(): T {
    return mapNotNull { it.safeGetObj() }.firstObjectOf<T>()
}

inline fun <reified T: Object> Collection<PPtr<*>>.firstObjectOf(): T {
    return mapNotNull { it.safeGetObj() }.firstObjectOf<T>()
}

inline fun <reified T: Object> Array<out PPtr<*>>.firstOfOrNull(): T? {
    return mapNotNull { it.safeGetObj() }.firstOfOrNull()
}

inline fun <reified T: Object> Collection<PPtr<*>>.firstOfOrNull(): T? {
    return mapNotNull { it.safeGetObj() }.firstOfOrNull()
}
