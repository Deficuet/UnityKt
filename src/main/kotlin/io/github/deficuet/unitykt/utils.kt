package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object

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

inline fun <reified O: Object> List<Object>.allInstanceOf(): List<O> {
    return mutableListOf<O>().apply {
        for (obj in this@allInstanceOf) {
            if (obj is O) add(obj)
        }
    }
}

fun List<Object>.allInstanceOf(vararg type: String): List<Object> {
    return mutableListOf<Object>().apply {
        for (obj in this@allInstanceOf) {
            if (obj.type.name in type) {
                add(obj)
            }
        }
    }
}