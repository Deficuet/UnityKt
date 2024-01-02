package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.firstObjectOf
import io.github.deficuet.unitykt.firstOfOrNull
import io.github.deficuet.unitykt.internal.impl.PPtrImpl
import io.github.deficuet.unitykt.internal.impl.getObj
import io.github.deficuet.unitykt.internal.impl.safeGetObj
import io.github.deficuet.unitykt.safeCast

interface PPtr<out T: UnityObject> {
    val mFileID: Int
    val mPathID: Long
    val isNull: Boolean
}

inline fun <reified T: UnityObject> PPtr<T>.safeGetObj() = (this as PPtrImpl<T>).safeGetObj()

inline fun <reified T: UnityObject> PPtr<T>.getObj(): T = (this as PPtrImpl<T>).getObj()

inline fun <reified T: UnityObject> PPtr<*>.safeGetAs(): T? = (this as PPtrImpl<UnityObject>).safeGetObj() as? T

inline fun <reified T: UnityObject> PPtr<*>.getAs(): T = (this as PPtrImpl<UnityObject>).getObj() as T

inline fun <reified O: UnityObject> Iterable<PPtr<*>>.firstObjectOf() =
    mapNotNull { it.safeGetObj() }.firstObjectOf<O>()

inline fun <reified O: UnityObject> Array<out PPtr<*>>.firstObjectOf() =
    mapNotNull { it.safeGetObj() }.firstObjectOf<O>()

inline fun <reified O: UnityObject> Iterable<PPtr<*>>.firstOfOrNull(): O? {
    return mapNotNull { it.safeGetObj() }.firstOfOrNull()
}

inline fun <reified O: UnityObject> Array<out PPtr<*>>.firstOfOrNull(): O? {
    return mapNotNull { it.safeGetObj() }.firstOfOrNull()
}

inline fun <reified T: UnityObject> Iterable<PPtr<*>>.safeFindWithPathID(pathId: Long): T? {
    return find { it.mPathID == pathId }?.safeGetObj().safeCast()
}

inline fun <reified T: UnityObject> Array<out PPtr<*>>.safeFindWithPathID(pathId: Long): T? {
    return find { it.mPathID == pathId }?.safeGetObj().safeCast()
}

inline fun <reified T: UnityObject> Iterable<PPtr<*>>.findWithPathID(pathId: Long): T {
    return first { it.mPathID == pathId }.getObj().cast()
}

inline fun <reified T: UnityObject> Array<PPtr<*>>.findWithPathID(pathId: Long): T {
    return first { it.mPathID == pathId }.getObj().cast()
}
