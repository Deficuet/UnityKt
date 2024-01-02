package io.github.deficuet.unitykt.classes

interface SkinnedMeshRenderer: Renderer {
    val mMesh: PPtr<Mesh>
    val mBones: Array<out PPtr<Transform>>
    val mBlendShapeWeights: FloatArray
}