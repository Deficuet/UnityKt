package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object

operator fun List<ImportContext>.get(key: String) = find { it.name.contentEquals(key) }

operator fun <K, V> Collection<Pair<K, V>>.get(key: K): List<V> {
    return filter {
        when (val f = it.first) {
            is String -> f.contentEquals(key as String)
            is Array<*> -> f.contentEquals(key as Array<*>)
            else -> f == key
        }
    }.map { it.second }
}

operator fun <K, V> Array<Pair<K, V>>.get(key: K): List<V> {
    return filter {
        when (val f = it.first) {
            is String -> f.contentEquals(key as String)
            is Array<*> -> f.contentEquals(key as Array<*>)
            else -> f == key
        }
    }.map { it.second }
}

fun <K, V> List<Pair<K, V>>.first(key: K) = get(key)[0]

fun <K, V> List<Pair<K, V>>.firstOrNull(key: K) = with(get(key)) { if (isEmpty()) null else this[0] }

inline fun <reified O: Object> List<Object>.allObjectsOf(): List<O> {
    return mutableListOf<O>().apply {
        for (obj in this@allObjectsOf) {
            if (obj is O) add(obj)
        }
    }
}

inline fun <reified O: Object> List<Object>.firstObjectOf() = allObjectsOf<O>()[0]

inline fun <reified O: Object> List<Object>.firstOfOrNull(): O? {
    return with(allObjectsOf<O>()) { if (isEmpty()) null else this[0] }
}

fun List<Object>.allObjectsOf(vararg type: String): List<Object> {
    return mutableListOf<Object>().apply {
        for (obj in this@allObjectsOf) {
            if (obj.type.name in type) {
                add(obj)
            }
        }
    }
}

fun List<Object>.objectFromPathID(pathId: Long): Object? {
    return firstOrNull { it.mPathID == pathId }
}
