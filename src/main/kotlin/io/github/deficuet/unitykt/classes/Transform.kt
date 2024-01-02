package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Quaternion
import io.github.deficuet.unitykt.math.Vector3

interface Transform: Component {
    val mLocalRotation: Quaternion
    val mLocalPosition: Vector3
    val mLocalScale: Vector3
    val mChildren: Array<out PPtr<Transform>>
    val mFather: PPtr<Transform>
}