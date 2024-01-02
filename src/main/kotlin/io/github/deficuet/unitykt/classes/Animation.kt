package io.github.deficuet.unitykt.classes

interface Animation: Behaviour {
    val mAnimations: Array<out PPtr<AnimationClip>>
}