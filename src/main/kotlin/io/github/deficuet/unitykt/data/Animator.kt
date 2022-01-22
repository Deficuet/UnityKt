package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AnimatorImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Animator private constructor(
    private val container: ImplementationContainer<AnimatorImpl>
): Behaviour(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AnimatorImpl(ObjectReader(assetFile, info)) })

    val mAvatar: PPtr<Avatar>                           get() = container.impl.mAvatar
    val mController: PPtr<RuntimeAnimatorController>    get() = container.impl.mController
    val mHasTransformHierarchy: Boolean                 get() = container.impl.mHasTransformHierarchy
}