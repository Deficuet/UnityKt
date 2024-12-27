package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.metadata.UnityObjectCompanion
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata

inline fun <reified T> Any?.cast() = this as T

inline fun <reified T> Any?.safeCast() = this as? T

fun <T: UnityObject> Map<Long, UnityObjectMetadata>.safeGetAs(pathId: Long, loader: UnityObjectCompanion<T>): T? {
    return this[pathId]?.let { loader.load(it) }
}

fun <T: UnityObject> Map<Long, UnityObjectMetadata>.getAs(pathId: Long, loader: UnityObjectCompanion<T>): T {
    return loader.load(getValue(pathId))
}
