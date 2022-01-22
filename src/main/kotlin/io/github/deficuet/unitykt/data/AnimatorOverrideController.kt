package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AnimationClipOverride
import io.github.deficuet.unitykt.dataImpl.AnimatorOverrideControllerImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AnimatorOverrideController private constructor(
    private val container: ImplementationContainer<AnimatorOverrideControllerImpl>
): RuntimeAnimatorController(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AnimatorOverrideControllerImpl(ObjectReader(assetFile, info)) })

    val mController: PPtr<RuntimeAnimatorController>    get() = container.impl.mController
    val mClips: Array<AnimationClipOverride>            get() = container.impl.mClips
}