package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.data.UnityDataClass
import io.github.deficuet.unitykt.data.UnityObject
import kotlin.Array
import kotlin.Int
import java.lang.reflect.Array as JArray

abstract class UnityDataClassCompanion<D: UnityDataClass>(
    private val clazz: Class<D>,
    private val loader: ((UnityObject) -> D)? = null
) {
    open fun load(obj: UnityObject): D {
        return loader!!(obj)
    }

    internal fun createArray(size: Int): Array<*> {
        return JArray.newInstance(clazz, size) as Array<*>
    }
}
