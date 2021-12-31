package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class AnimatorOverrideController internal constructor(reader: ObjectReader): RuntimeAnimatorController(reader) {
    val mController = PPtr<RuntimeAnimatorController>(reader)
    val mClips = reader.readArrayOf { AnimationClipOverride(reader) }
}

class AnimationClipOverride internal constructor(reader: ObjectReader) {
    val mOriginalClip = PPtr<AnimationClip>(reader)
    val mOverrideClip = PPtr<AnimationClip>(reader)
}