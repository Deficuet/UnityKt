package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.data.UnityObject

abstract class DataBinding internal constructor() {
    internal val propertyMap = mutableMapOf<String, Any>()

    protected fun <T: UnityObject> bind(
        propertyName: String,
        loader: UnityObjectCompanion<T>
    ) {

    }
}
