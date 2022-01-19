package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.RendererImpl

abstract class Renderer internal constructor(
    private val container: ImplementationContainer<RendererImpl>
): Component(container) {
    val mMaterials get() = container.impl.mMaterials
    val mStaticBatchInfo get() = container.impl.mStaticBatchInfo
    val mSubsetIndices get() = container.impl.mSubsetIndices
}