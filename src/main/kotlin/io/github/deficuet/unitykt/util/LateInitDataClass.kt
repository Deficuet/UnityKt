package io.github.deficuet.unitykt.util

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

abstract class LateInitDataClass {
    protected abstract val map: MutableMap<String, Any>

    operator fun <T> set(prop: KProperty<T>, value: T) {
        if ((prop.name in map) imply (prop is KMutableProperty<*>)) map[prop.name] = value!!
    }
}