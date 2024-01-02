package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.readArrayOf

internal class AnimatorOverrideControllerImpl(
    assetFile: SerializedFile, info: ObjectInfo
): AnimatorOverrideController, AnimatorOverrideControllerFields(assetFile, info) {
    override val mController: PPtr<RuntimeAnimatorController> get() {
        checkInitialize()
        return fmController
    }
    override val mClips: Array<out AnimationClipOverride> get() {
        checkInitialize()
        return fmClips
    }

    override fun read() {
        super.read()
        fmController = PPtrImpl(reader)
        fmClips = reader.readArrayOf { AnimationClipOverrideImpl(this) }
    }
}

internal class AnimationClipOverrideImpl(reader: ObjectReader): AnimationClipOverride {
    override val mOriginalClip = PPtrImpl<AnimationClip>(reader)
    override val mOverrideClip = PPtrImpl<AnimationClip>(reader)
}
