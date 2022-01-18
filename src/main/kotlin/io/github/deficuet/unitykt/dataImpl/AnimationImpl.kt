package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

class AnimationImpl(reader: ObjectReader): BehaviourImpl(reader) {
    val mAnimations: Array<PPtr<AnimationClipImpl>>

    init {
        PPtr<AnimationClipImpl>(reader)     //m_Animation
        mAnimations = reader.readArrayOf { PPtr(reader) }
    }
}