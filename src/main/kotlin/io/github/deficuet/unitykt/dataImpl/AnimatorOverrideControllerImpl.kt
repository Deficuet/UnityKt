package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

class AnimatorOverrideControllerImpl internal constructor(reader: ObjectReader): RuntimeAnimatorControllerImpl(reader) {
    val mController = PPtr<RuntimeAnimatorControllerImpl>(reader)
    val mClips = reader.readArrayOf { AnimationClipOverride(reader) }
}

class AnimationClipOverride internal constructor(reader: ObjectReader) {
    val mOriginalClip = PPtr<AnimationClipImpl>(reader)
    val mOverrideClip = PPtr<AnimationClipImpl>(reader)
}