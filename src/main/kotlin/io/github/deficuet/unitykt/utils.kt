package io.github.deficuet.unitykt

inline fun <reified T> Any?.cast() = this as T

inline fun <reified T> Any?.safeCast() = this as? T
