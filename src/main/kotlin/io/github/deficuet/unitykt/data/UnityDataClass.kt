package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.metadata.DataBinding

abstract class UnityDataClass(protected val valueMap: Map<String, Any>): DataBinding() {
}
