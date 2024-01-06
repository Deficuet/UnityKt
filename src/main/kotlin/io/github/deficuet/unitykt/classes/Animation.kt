package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface Animation: Behaviour {
    val mAnimations: Array<out PPtr<AnimationClip>>
}