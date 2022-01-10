package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class GameObject internal constructor(reader: ObjectReader): EditorExtension(reader) {
    val mComponents: Array<PPtr<Component>>
    val mName: String
    val mTransform = mutableListOf<Transform>()
    val mMeshRenderer = mutableListOf<MeshRenderer>()
    val mMeshFilter = mutableListOf<MeshFilter>()
    val mSkinnedMeshRenderer = mutableListOf<SkinnedMeshRenderer>()
    val mAnimator = mutableListOf<Animator>()
    val mAnimation = mutableListOf<Animation>()

    init {
//        val componentSize = reader.readInt()
//        val components = mutableListOf<PPtr<Component>>()
//        for (i in 0 until componentSize) {
//            if (unityVersion < intArrayOf(5, 5)) {
//                reader += 4     //first: Int
//            }
//            components.add(PPtr(reader))
//        }
        val components = reader.readArrayOf {
            if (unityVersion < intArrayOf(5, 5)) {
                reader += 4     //first: Int
            }
            PPtr<Component>(reader)
        }
        reader += 4     //m_Layer: Int
        mName = reader.readAlignedString()
        mComponents = components
    }
}