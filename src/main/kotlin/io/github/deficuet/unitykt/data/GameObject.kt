package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.GameObjectImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class GameObject private constructor(
    private val container: ImplementationContainer<GameObjectImpl>
): EditorExtension(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { GameObjectImpl(ObjectReader(assetFile, info)) })

    val mComponents: Array<PPtr<Component>>                 get() = container.impl.mComponents
    val mName: String                                       get() = container.impl.mName
    val mTransform: Array<Transform>                        get() = container.impl.mTransform
    val mMeshRenderer: Array<MeshRenderer>                  get() = container.impl.mMeshRenderer
    val mMeshFilter: Array<MeshFilter>                      get() = container.impl.mMeshFilter
    val mSkinnedMeshRenderer: Array<SkinnedMeshRenderer>    get() = container.impl.mSkinnedMeshRenderer
    val mAnimator: Array<Animator>                          get() = container.impl.mAnimator
    val mAnimation: Array<Animation>                        get() = container.impl.mAnimation
}