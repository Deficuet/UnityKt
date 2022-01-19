package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class GameObjectImpl internal constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mComponents: Array<PPtr<ComponentImpl>>
    val mName: String
    val mTransform = mutableListOf<TransformImpl>()
    val mMeshRenderer = mutableListOf<MeshRendererImpl>()
    val mMeshFilter = mutableListOf<MeshFilterImpl>()
    val mSkinnedMeshRenderer = mutableListOf<SkinnedMeshRendererImpl>()
    val mAnimator = mutableListOf<AnimatorImpl>()
    val mAnimation = mutableListOf<AnimationImpl>()

    init {
        val components = reader.readArrayOf {
            if (unityVersion < intArrayOf(5, 5)) {
                reader += 4     //first: Int
            }
            PPtr<ComponentImpl>(reader)
        }
        reader += 4     //m_Layer: Int
        mName = reader.readAlignedString()
        mComponents = components
        for (pptr in mComponents) {
            val obj = pptr.obj
            if (obj != null) {
                when (obj) {
                    is TransformImpl -> mTransform.add(obj)
                    is MeshRendererImpl -> mMeshRenderer.add(obj)
                    is MeshFilterImpl -> mMeshFilter.add(obj)
                    is SkinnedMeshRendererImpl -> mSkinnedMeshRenderer.add(obj)
                    is AnimatorImpl -> mAnimator.add(obj)
                    is AnimationImpl -> mAnimation.add(obj)
                }
            }
        }
    }
}