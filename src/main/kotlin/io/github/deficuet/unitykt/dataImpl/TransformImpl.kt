package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

open class TransformImpl internal constructor(reader: ObjectReader): ComponentImpl(reader) {
    val mLocalRotation = reader.readQuaternion()
    val mLocalPosition = reader.readVector3()
    val mLocalScale = reader.readVector3()
    val mChildren = reader.readArrayOf { PPtr<TransformImpl>(reader) }
    val mFather = PPtr<TransformImpl>(reader)
}