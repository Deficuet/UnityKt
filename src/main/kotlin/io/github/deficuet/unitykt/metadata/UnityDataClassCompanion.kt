package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.data.UnityDataClass
import io.github.deficuet.unitykt.data.UnityObject

abstract class UnityDataClassCompanion<D: UnityDataClass>(
    internal val clazz: Class<D>,
    private val loader: ((UnityObject) -> D)? = null
) {
    open fun load(obj: UnityObject): D {
        return loader!!(obj)
    }
}
