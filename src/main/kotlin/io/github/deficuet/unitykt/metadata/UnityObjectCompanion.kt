package io.github.deficuet.unitykt.metadata

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.enums.ClassIDType

abstract class UnityObjectCompanion<T: UnityObject>(
    val classType: ClassIDType,
    private val loader: (UnityObjectMetadata) -> T
) {

}
