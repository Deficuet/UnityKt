package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.enums.ClassIDType

/**
 * Use [filterIsInstance] for finding all objects of a specific type.
 */
inline fun <reified O: UnityObject> Iterable<UnityObject>.firstObjectOf() = filterIsInstance<O>()[0]

inline fun <reified O: UnityObject> Array<out UnityObject>.firstObjectOf() = filterIsInstance<O>()[0]

inline fun <reified O: UnityObject> Iterable<UnityObject>.firstOfOrNull(): O? {
    return with(filterIsInstance<O>()) { if (isEmpty()) null else this[0] }
}

inline fun <reified O: UnityObject> Array<out UnityObject>.firstOfOrNull(): O? {
    return with(filterIsInstance<O>()) { if (isEmpty()) null else this[0] }
}

fun Iterable<UnityObject>.allObjectsOf(vararg types: ClassIDType): List<UnityObject> {
    return filter { it.type in types }
}

fun Array<UnityObject>.allObjectsOf(vararg types: ClassIDType): List<UnityObject> {
    return filter { it.type in types }
}

inline fun <reified T: UnityObject> Iterable<UnityObject>.safeFindWithPathID(pathId: Long): T? {
    return find { it.mPathID == pathId }.safeCast()
}

inline fun <reified T: UnityObject> Array<out UnityObject>.safeFindWithPathID(pathId: Long): T? {
    return find { it.mPathID == pathId }.safeCast()
}

inline fun <reified T: UnityObject> Iterable<UnityObject>.findWithPathID(pathId: Long): T {
    return first { it.mPathID == pathId }.cast()
}

inline fun <reified T: UnityObject> Array<out UnityObject>.findWithPathID(pathId: Long): T {
    return first { it.mPathID == pathId }.cast()
}

inline fun <reified T: UnityObject> Map<Long, UnityObject>.safeGetAs(pathId: Long): T? {
    return this[pathId].safeCast()
}

inline fun <reified T: UnityObject> Map<Long, UnityObject>.getAs(pathId: Long): T {
    return this[pathId].cast()
}

inline fun <reified T> Any?.safeCast(): T? = this as? T

inline fun <reified T> Any?.cast(): T = this as T