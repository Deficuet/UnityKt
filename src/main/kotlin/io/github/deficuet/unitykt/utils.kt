package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.metadata.UnityObjectCompanion
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata

inline fun <reified T> Any?.cast() = this as T

inline fun <reified T> Any?.safeCast() = this as? T

fun <T: UnityObject> Map<Long, UnityObjectMetadata>.safeGetAs(loader: UnityObjectCompanion<T>, pathId: Long): T? {
    return this[pathId]?.let { loader.load(it) }
}

fun <T: UnityObject> Map<Long, UnityObjectMetadata>.getAs(loader: UnityObjectCompanion<T>, pathId: Long): T {
    return loader.load(getValue(pathId))
}
