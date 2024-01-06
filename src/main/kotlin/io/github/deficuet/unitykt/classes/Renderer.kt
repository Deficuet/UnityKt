package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface Renderer: Component {
    val mMaterials: Array<out PPtr<Material>>
    val mStaticBatchInfo: StaticBatchInfo?
    val mSubsetIndices: Array<out UInt>
}

interface StaticBatchInfo {
    val firstSubMesh: UShort
    val subMeshCount: UShort
}
