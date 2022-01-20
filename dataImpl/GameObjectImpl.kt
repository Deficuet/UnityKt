package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.*
import io.github.deficuet.unitykt.getObj
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class GameObjectImpl internal constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mComponents: Array<PPtr<Component>>
    val mName: String
    val mTransform = mutableListOf<Transform>()
    val mMeshRenderer = mutableListOf<MeshRenderer>()
    val mMeshFilter = mutableListOf<MeshFilter>()
    val mSkinnedMeshRenderer = mutableListOf<SkinnedMeshRenderer>()
    val mAnimator = mutableListOf<Animator>()
    val mAnimation = mutableListOf<Animation>()

    init {
        val components = reader.readArrayOf {
            if (unityVersion < intArrayOf(5, 5)) {
                reader += 4     //first: Int
            }
            PPtr<Component>(reader)
        }
        reader += 4     //m_Layer: Int
        mName = reader.readAlignedString()
        mComponents = components
        for (pptr in mComponents) {
            val obj = pptr.getObj()
            if (obj != null) {
                when (obj) {
                    is Transform -> mTransform.add(obj)
                    is MeshRenderer -> mMeshRenderer.add(obj)
                    is MeshFilter -> mMeshFilter.add(obj)
                    is SkinnedMeshRenderer -> mSkinnedMeshRenderer.add(obj)
                    is Animator -> mAnimator.add(obj)
                    is Animation -> mAnimation.add(obj)
                }
            }
        }
    }
}