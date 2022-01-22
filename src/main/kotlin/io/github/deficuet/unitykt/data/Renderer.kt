package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.RendererImpl
import io.github.deficuet.unitykt.dataImpl.StaticBatchInfo

abstract class Renderer internal constructor(
    private val container: ImplementationContainer<RendererImpl>
): Component(container) {
    val mMaterials: Array<PPtr<Material>>   get() = container.impl.mMaterials
    val mStaticBatchInfo: StaticBatchInfo?  get() = container.impl.mStaticBatchInfo
    val mSubsetIndices: Array<UInt>         get() = container.impl.mSubsetIndices
}