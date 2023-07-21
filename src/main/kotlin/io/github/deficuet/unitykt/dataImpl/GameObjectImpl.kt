package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.*
import io.github.deficuet.unitykt.safeGetObj
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class GameObjectImpl internal constructor(reader: ObjectReader): EditorExtensionImpl(reader) {
    val mComponents: Array<PPtr<Component>>
    val mName: String
    val mTransform: Array<Transform>
    val mMeshRenderer: Array<MeshRenderer>
    val mMeshFilter: Array<MeshFilter>
    val mSkinnedMeshRenderer: Array<SkinnedMeshRenderer>
    val mAnimator: Array<Animator>
    val mAnimation: Array<Animation>

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
        val transforms = mutableListOf<Transform>()
        val meshRenderers = mutableListOf<MeshRenderer>()
        val meshFilters = mutableListOf<MeshFilter>()
        val skinnedMeshRenderers = mutableListOf<SkinnedMeshRenderer>()
        val animators = mutableListOf<Animator>()
        val animations = mutableListOf<Animation>()
        for (pptr in mComponents) {
            val obj = pptr.safeGetObj()
            if (obj != null) {
                when (obj) {
                    is Transform -> transforms.add(obj)
                    is MeshRenderer -> meshRenderers.add(obj)
                    is MeshFilter -> meshFilters.add(obj)
                    is SkinnedMeshRenderer -> skinnedMeshRenderers.add(obj)
                    is Animator -> animators.add(obj)
                    is Animation -> animations.add(obj)
                }
            }
        }
        mTransform = transforms.toTypedArray()
        mMeshRenderer = meshRenderers.toTypedArray()
        mMeshFilter = meshFilters.toTypedArray()
        mSkinnedMeshRenderer = skinnedMeshRenderers.toTypedArray()
        mAnimator = animators.toTypedArray()
        mAnimation = animations.toTypedArray()
    }
}