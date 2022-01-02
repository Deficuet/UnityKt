package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

open class Transform internal constructor(reader: ObjectReader): Component(reader) {
    val mLocalRotation = reader.readQuaternion()
    val mLocalPosition = reader.readVector3()
    val mLocalScale = reader.readVector3()
    val mChildren = reader.readArrayOf { PPtr<Transform>(reader) }
    val mFather = PPtr<Transform>(reader)
}