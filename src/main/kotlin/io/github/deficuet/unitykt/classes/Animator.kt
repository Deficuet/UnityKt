package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface Animator: Behaviour {
    val mAvatar: PPtr<Avatar>
    val mController: PPtr<RuntimeAnimatorController>
    val mHasTransformHierarchy: Boolean
}