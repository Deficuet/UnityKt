package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.dataImpl.PPtr
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.StringRef
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

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

fun <V> Map<String, V>.tryGetOrUppercase(key: String): V? {
    return this[key] ?: this[key.uppercase()]
}

fun String.listFiles(): List<String> {
    return Files.newDirectoryStream(Path.of(this)).use { stream ->
        stream.filter { it.isRegularFile() }.map { it.name }
    }
}

fun List<String>.containsIgnoreCase(element: String, sRef: StringRef): Boolean {
    return find { it.contentEquals(element) }?.also { sRef.value = it } != null
}
