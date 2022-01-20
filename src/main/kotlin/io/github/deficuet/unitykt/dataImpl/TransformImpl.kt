package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Transform
import io.github.deficuet.unitykt.util.ObjectReader

open class TransformImpl internal constructor(reader: ObjectReader): ComponentImpl(reader) {
    val mLocalRotation = reader.readQuaternion()
    val mLocalPosition = reader.readVector3()
    val mLocalScale = reader.readVector3()
    val mChildren = reader.readArrayOf { PPtr<Transform>(reader) }
    val mFather = PPtr<Transform>(reader)
}