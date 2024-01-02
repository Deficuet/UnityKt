package io.github.deficuet.unitykt.classes

interface Animator: Behaviour {
    val mAvatar: PPtr<Avatar>
    val mController: PPtr<RuntimeAnimatorController>
    val mHasTransformHierarchy: Boolean
}