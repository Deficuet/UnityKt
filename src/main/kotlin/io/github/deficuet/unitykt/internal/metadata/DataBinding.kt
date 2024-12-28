package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.data.UnityDataClass
import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.internal.metadata.tree.Loader
import io.github.deficuet.unitykt.metadata.UnityDataClassCompanion
import kotlin.reflect.KProperty

abstract class DataBinding internal constructor() {
    internal val loaderMap = mutableMapOf<String, ArrayDeque<Loader>>()

    @PublishedApi
    internal val propertyMap = mutableMapOf<String, Any>()

    internal var isInitialized = false

    protected fun bind(dataKey: String, vararg loaders: UnityDataClassCompanion<*>?): String {
        if (loaders.isNotEmpty()) {
            val deque = ArrayDeque<Loader>(loaders.size)
            deque.addAll(loaders)
            loaderMap[dataKey] = deque
        }
        return dataKey
    }

    @PublishedApi
    internal fun checkInit(obj: UnityObject) {
        if (!obj.isInitialized) {
            obj.metadata.cast<UnityObjectMetadataImpl>().readTree(obj)
            obj.isInitialized = true
        }
    }

    inline fun <reified T> validateType(propertyName: String, dataKey: String): T {
        val value = propertyMap[dataKey]
            ?: throw NullPointerException("Property [${propertyName}](\"${dataKey}\") does not exist")
        if (value !is T) {
            throw IllegalStateException(
                "Property [${propertyName}](\"${dataKey}\") has incorrect type: ${value::class.qualifiedName}. " +
                        "Expect: ${T::class.qualifiedName}"
            )
        }
        return value
    }

    inline operator fun <reified T> String.getValue(thisRef: UnityObject, property: KProperty<*>): T {
        checkInit(thisRef)
        return validateType(property.name, this)
    }

    inline operator fun <reified T> String.getValue(thisRef: UnityDataClass, property: KProperty<*>): T {
        return validateType(property.name, this)
    }
}
