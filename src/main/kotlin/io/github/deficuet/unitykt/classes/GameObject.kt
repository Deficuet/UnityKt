package io.github.deficuet.unitykt.classes

interface GameObject: EditorExtension {
    val mComponents: Array<out PPtr<Component>>
    val mLayer: UInt
    val mName: String
    val mTag: UShort
    val mIsActive: Boolean
    val mTransform: Transform
    val mMeshRenderer: MeshRenderer
    val mMeshFilter: MeshFilter
    val mSkinnedMeshRenderer: SkinnedMeshRenderer
    val mAnimator: Animator
    val mAnimation: Animation
}