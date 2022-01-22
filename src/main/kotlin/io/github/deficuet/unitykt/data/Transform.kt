package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.TransformImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.math.Quaternion
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.util.ObjectReader

open class Transform internal constructor(
    private val container: ImplementationContainer<TransformImpl>
): Component(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { TransformImpl(ObjectReader(assetFile, info)) })

    val mLocalRotation: Quaternion          get() = container.impl.mLocalRotation
    val mLocalPosition: Vector3             get() = container.impl.mLocalPosition
    val mLocalScale: Vector3                get() = container.impl.mLocalScale
    val mChildren: Array<PPtr<Transform>>   get() = container.impl.mChildren
    val mFather: PPtr<Transform>            get() = container.impl.mFather
}