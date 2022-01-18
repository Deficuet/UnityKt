package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.dataImpl.ObjectImpl

operator fun List<ImportContext>.get(key: String) = find { it.name.contentEquals(key) }

operator fun <K, V> List<Pair<K, V>>.get(key: K): List<V> {
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

inline fun <reified O: ObjectImpl> List<ObjectImpl>.allObjectsOf(): List<O> {
    return mutableListOf<O>().apply {
        for (obj in this@allObjectsOf) {
            if (obj is O) add(obj)
        }
    }
}

inline fun <reified O: ObjectImpl> List<ObjectImpl>.firstObjectOf() = allObjectsOf<O>()[0]

inline fun <reified O: ObjectImpl> List<ObjectImpl>.firstOfOrNull(): O? {
    return with(allObjectsOf<O>()) { if (isEmpty()) null else this[0] }
}

fun List<ObjectImpl>.allInstanceOf(vararg type: String): List<ObjectImpl> {
    return mutableListOf<ObjectImpl>().apply {
        for (obj in this@allInstanceOf) {
            if (obj.type.name in type) {
                add(obj)
            }
        }
    }
}

fun List<ObjectImpl>.objectFromPathID(pathId: Long): ObjectImpl? {
    return firstOrNull { it.mPathID == pathId }
}
