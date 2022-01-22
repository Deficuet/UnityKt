package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.TransformImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

open class Transform internal constructor(
    private val container: ImplementationContainer<TransformImpl>
): Component(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { TransformImpl(ObjectReader(assetFile, info)) })

    val mLocalRotation get() = container.impl.mLocalRotation
    val mLocalPosition get() = container.impl.mLocalPosition
    val mLocalScale get() = container.impl.mLocalScale
    val mChildren get() = container.impl.mChildren
    val mFather get() = container.impl.mFather
}