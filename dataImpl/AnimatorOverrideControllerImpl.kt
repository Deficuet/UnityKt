package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.AnimationClip
import io.github.deficuet.unitykt.data.RuntimeAnimatorController
import io.github.deficuet.unitykt.util.ObjectReader

class AnimatorOverrideControllerImpl internal constructor(reader: ObjectReader): RuntimeAnimatorControllerImpl(reader) {
    val mController = PPtr<RuntimeAnimatorController>(reader)
    val mClips = reader.readArrayOf { AnimationClipOverride(reader) }
}

class AnimationClipOverride internal constructor(reader: ObjectReader) {
    val mOriginalClip = PPtr<AnimationClip>(reader)
    val mOverrideClip = PPtr<AnimationClip>(reader)
}