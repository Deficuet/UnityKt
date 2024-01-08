package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Mesh
import io.github.deficuet.unitykt.classes.MeshFilter
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.pptr.PPtr

internal class MeshFilterImpl(
    assetFile: SerializedFile, info: ObjectInfo
): MeshFilter, MeshFilterFields(assetFile, info) {
    override val mMesh: PPtr<Mesh> get() {
        checkInitialize()
        return fmMesh
    }

    override fun read() {
        super.read()
        fmMesh = PPtrImpl(reader)
    }
}