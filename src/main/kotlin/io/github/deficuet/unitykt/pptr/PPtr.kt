package io.github.deficuet.unitykt.pptr

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.firstObjectOf
import io.github.deficuet.unitykt.firstOfOrNull
import io.github.deficuet.unitykt.internal.impl.PPtrImpl
import io.github.deficuet.unitykt.internal.impl.getObjInternal
import io.github.deficuet.unitykt.internal.impl.safeGetObjInternal
import io.github.deficuet.unitykt.safeCast

interface PPtr<out T: UnityObject> {
    val mFileID: Int
    val mPathID: Long
    val isNull: Boolean
}

inline fun <reified T: UnityObject> PPtr<T>.safeGetObj(): T? {
    return (this as PPtrImpl<T>).safeGetObjInternal()
}

inline fun <reified T: UnityObject> PPtr<T>.getObj(): T {
    return (this as PPtrImpl<T>).getObjInternal()
}

inline fun <reified T: UnityObject> PPtr<*>.safeGetAs(): T? {
    return (this as PPtrImpl<UnityObject>).safeGetObjInternal().safeCast()
}

inline fun <reified T: UnityObject> PPtr<*>.getAs(): T {
    return (this as PPtrImpl<UnityObject>).getObjInternal().cast()
}

inline fun <reified T: UnityObject> Iterable<PPtr<*>>.firstObjectOf(): T {
    return mapNotNull { it.safeGetObj() }.firstObjectOf<T>()
}

inline fun <reified T: UnityObject> Array<out PPtr<*>>.firstObjectOf(): T {
    return mapNotNull { it.safeGetObj() }.firstObjectOf<T>()
}

inline fun <reified T: UnityObject> Iterable<PPtr<*>>.firstOfOrNull(): T? {
    return mapNotNull { it.safeGetObj() }.firstOfOrNull()
}

inline fun <reified T: UnityObject> Array<out PPtr<*>>.firstOfOrNull(): T? {
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
