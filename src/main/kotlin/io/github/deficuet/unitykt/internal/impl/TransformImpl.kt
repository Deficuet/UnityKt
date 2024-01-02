package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.PPtr
import io.github.deficuet.unitykt.classes.Transform
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Quaternion
import io.github.deficuet.unitykt.math.Vector3
import io.github.deficuet.unitykt.util.readArrayOf

internal open class TransformImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Transform, TransformFields(assetFile, info) {
    final override val mLocalRotation: Quaternion get() {
        checkInitialize()
        return fmLocalRotation
    }
    final override val mLocalPosition: Vector3 get() {
        checkInitialize()
        return fmLocalPosition
    }
    final override val mLocalScale: Vector3 get() {
        checkInitialize()
        return fmLocalScale
    }
    final override val mChildren: Array<out PPtr<Transform>> get() {
        checkInitialize()
        return fmChildren
    }
    final override val mFather: PPtr<Transform> get() {
        checkInitialize()
        return fmFather
    }

    override fun read() {
        super.read()
        fmLocalRotation = reader.readQuaternion()
        fmLocalPosition = reader.readVector3()
        fmLocalScale = reader.readVector3()
        fmChildren = reader.readArrayOf { PPtrImpl(this) }
        fmFather = PPtrImpl(reader)
    }
}
