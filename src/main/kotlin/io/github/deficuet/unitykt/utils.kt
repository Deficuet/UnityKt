package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object

operator fun <K, V> Collection<Pair<K, V>>.get(key: K): List<V> {
    return filter {
        when (val f = it.first) {
            is CharSequence -> f.contentEquals(key as CharSequence)
            is Array<*> -> f.contentEquals(key as Array<*>)
            is ByteArray -> f.contentEquals(key as ByteArray)
            is CharArray -> f.contentEquals(key as CharArray)
            is ShortArray -> f.contentEquals(key as ShortArray)
            is IntArray -> f.contentEquals(key as IntArray)
            is LongArray -> f.contentEquals(key as LongArray)
            is DoubleArray -> f.contentEquals(key as DoubleArray)
            is FloatArray -> f.contentEquals(key as FloatArray)
            is BooleanArray -> f.contentEquals(key as BooleanArray)
            else -> f == key
        }
    }.map { it.second }
}

operator fun <K, V> Array<Pair<K, V>>.get(key: K): List<V> {
    return filter {
        when (val f = it.first) {
            is CharSequence -> f.contentEquals(key as CharSequence)
            is Array<*> -> f.contentEquals(key as Array<*>)
            is ByteArray -> f.contentEquals(key as ByteArray)
            is CharArray -> f.contentEquals(key as CharArray)
            is ShortArray -> f.contentEquals(key as ShortArray)
            is IntArray -> f.contentEquals(key as IntArray)
            is LongArray -> f.contentEquals(key as LongArray)
            is DoubleArray -> f.contentEquals(key as DoubleArray)
            is FloatArray -> f.contentEquals(key as FloatArray)
            is BooleanArray -> f.contentEquals(key as BooleanArray)
            else -> f == key
        }
    }.map { it.second }
}

fun <K, V> Collection<Pair<K, V>>.first(key: K) = get(key)[0]

fun <K, V> Collection<Pair<K, V>>.firstOrNull(key: K) = with(get(key)) { if (isEmpty()) null else this[0] }

inline fun <reified O: Object> Collection<Object>.firstObjectOf() = filterIsInstance<O>()[0]

inline fun <reified O: Object> Collection<Object>.firstOfOrNull(): O? {
    return with(filterIsInstance<O>()) { if (isEmpty()) null else this[0] }
}

fun Collection<Object>.allObjectsOf(vararg type: String): List<Object> {
    return filter { it.type.name in type }
}

inline fun <reified T: Object> Collection<Object>.safeFindWithPathID(pathId: Long): T? {
    return with(first { it.mPathID == pathId }) { if (this is T) this else null }
}

inline fun <reified T: Object> Collection<Object>.findWithPathID(pathId: Long): T {
    return first { it.mPathID == pathId } as T
}

inline fun <reified T: Object> Map<Long, Object>.safeGetAs(pathId: Long): T? {
    return this[pathId].safeCast()
}

inline fun <reified T: Object> Map<Long, Object>.getAs(pathId: Long): T {
    return this[pathId].cast()
}

inline fun <reified T> Any?.safeCast(): T? = this as? T

inline fun <reified T> Any?.cast(): T = this as T