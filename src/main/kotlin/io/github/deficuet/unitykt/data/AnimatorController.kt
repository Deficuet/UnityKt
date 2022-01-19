package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AnimatorControllerImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AnimatorController private constructor(
    private val container: ImplementationContainer<AnimatorControllerImpl>
): RuntimeAnimatorController(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { AnimatorControllerImpl(ObjectReader(assetFile, info)) })

    val mAnimationClips get() = container.impl.mAnimationClips
}