package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Animation
import io.github.deficuet.unitykt.classes.Mesh
import io.github.deficuet.unitykt.classes.SkinnedMeshRenderer
import io.github.deficuet.unitykt.classes.Transform
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class SkinnedMeshRendererImpl(
    assetFile: SerializedFile, info: ObjectInfo
): SkinnedMeshRenderer, SkinnedMeshRendererFields(assetFile, info) {
    override val mMesh: PPtr<Mesh> get() {
        checkInitialize()
        return fmMesh
    }
    override val mBones: Array<out PPtr<Transform>> get() {
        checkInitialize()
        return fmBones
    }
    override val mBlendShapeWeights: FloatArray get() {
        checkInitialize()
        return fmBlendShapeWeights
    }

    override fun read() {
        super.read()
        reader.skip(6)     //m_Quality: Int, m_UpdateWhenOffscreen, m_SkinNormals: Boolean
        reader.alignStream()
        if (unityVersion[0] == 2 && unityVersion[1] < 6) {
            PPtrImpl<Animation>(reader)     //m_DisableAnimationWhenOffscreen
        }
        fmMesh = PPtrImpl(reader)
        fmBones = reader.readArrayOf { PPtrImpl(this) }
        fmBlendShapeWeights = if (unityVersion >= intArrayOf(4, 3)) {
            reader.readFloatArray()
        } else FloatArray(0)
    }
}