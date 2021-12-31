package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class Animation(reader: ObjectReader): Behaviour(reader) {
    val mAnimations: List<PPtr<AnimationClip>>

    init {
        PPtr<AnimationClip>(reader)     //m_Animation
//        val numAnimations = reader.readInt()
//        val animations = mutableListOf<PPtr<AnimationClip>>()
//        for (i in 0 until numAnimations) {
//            animations.add(PPtr(reader))
//        }
        mAnimations = reader.readArrayOf { PPtr(reader) }
    }
}