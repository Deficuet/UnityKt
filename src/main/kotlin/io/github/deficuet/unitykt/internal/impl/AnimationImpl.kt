package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Animation
import io.github.deficuet.unitykt.classes.AnimationClip
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.util.readArrayOf

internal class AnimationImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Animation, AnimationFields(assetFile, info) {
    override val mAnimations: Array<out PPtr<AnimationClip>> get() {
        checkInitialize()
        return fmAnimations
    }

    override fun read() {
        super.read()
        PPtrImpl<AnimationClip>(reader)
        fmAnimations = reader.readArrayOf { PPtrImpl(this) }
    }
}