package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AnimationImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Animation private constructor(
    private val container: ImplementationContainer<AnimationImpl>
): Behaviour(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AnimationImpl(ObjectReader(assetFile, info)) })

    val mAnimations: Array<PPtr<AnimationClip>> get() = container.impl.mAnimations
}