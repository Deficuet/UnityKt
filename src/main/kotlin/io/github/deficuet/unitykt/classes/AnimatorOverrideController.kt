package io.github.deficuet.unitykt.classes

interface AnimatorOverrideController: RuntimeAnimatorController {
    val mController: PPtr<RuntimeAnimatorController>
    val mClips: Array<out AnimationClipOverride>
}

interface AnimationClipOverride {
    val mOriginalClip: PPtr<AnimationClip>
    val mOverrideClip: PPtr<AnimationClip>
}
