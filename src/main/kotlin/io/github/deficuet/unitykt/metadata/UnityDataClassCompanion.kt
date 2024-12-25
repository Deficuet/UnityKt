package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.data.UnityDataClass
import io.github.deficuet.unitykt.data.UnityObject
import kotlin.reflect.KProperty

abstract class UnityDataClassCompanion<D: UnityDataClass>(
    private val loader: (Map<String, Any>) -> D
) {

    operator fun getValue(obj: UnityObject, property: KProperty<*>): D {

        return null as D
    }
}