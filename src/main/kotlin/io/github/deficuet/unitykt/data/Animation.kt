package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class Animation(reader: ObjectReader): Behaviour(reader) {
    val mAnimations: List<PPtr<AnimationClip>>

    init {
        PPtr<AnimationClip>(reader)     //m_Animation
        mAnimations = reader.readArrayOf { PPtr(reader) }
    }
}