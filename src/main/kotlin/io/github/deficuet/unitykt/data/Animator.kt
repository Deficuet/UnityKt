package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AnimatorImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Animator private constructor(
    private val container: ImplementationContainer<AnimatorImpl>
): Behaviour(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { AnimatorImpl(ObjectReader(assetFile, info)) })

    val mAvatar get() = container.impl.mAvatar
    val mController get() = container.impl.mController
    val mHasTransformHierarchy get() = container.impl.mHasTransformHierarchy
}