package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface SkinnedMeshRenderer: Renderer {
    val mMesh: PPtr<Mesh>
    val mBones: Array<out PPtr<Transform>>
    val mBlendShapeWeights: FloatArray
}