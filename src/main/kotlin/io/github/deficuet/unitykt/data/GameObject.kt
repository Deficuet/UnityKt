package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.GameObjectImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class GameObject private constructor(
    private val container: ImplementationContainer<GameObjectImpl>
): EditorExtension(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { GameObjectImpl(ObjectReader(assetFile, info)) })

    val mComponents get() = container.impl.mComponents
    val mName get() = container.impl.mName
    val mTransform get() = container.impl.mTransform
    val mMeshRenderer get() = container.impl.mMeshRenderer
    val mMeshFilter get() = container.impl.mMeshFilter
    val mSkinnedMeshRenderer get() = container.impl.mSkinnedMeshRenderer
    val mAnimator get() = container.impl.mAnimator
    val mAnimation get() = container.impl.mAnimation
}