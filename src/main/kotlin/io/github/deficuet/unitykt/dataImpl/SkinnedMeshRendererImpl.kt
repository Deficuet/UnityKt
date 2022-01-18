package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class SkinnedMeshRendererImpl internal constructor(reader: ObjectReader): RendererImpl(reader) {
    val mMesh: PPtr<MeshImpl>
    val mBones: Array<PPtr<TransformImpl>>
    val mBlendShapeWeights: FloatArray

    init {
        reader += 6     //m_Quality: Int, m_UpdateWhenOffscreen, m_SkinNormals: Boolean
        reader.alignStream()
        if (unityVersion[0] == 2 && unityVersion[1] < 6) {
            PPtr<AnimationImpl>(reader)     //m_DisableAnimationWhenOffscreen
        }
        mMesh = PPtr(reader)
        mBones = reader.readArrayOf { PPtr(reader) }
        mBlendShapeWeights = if (unityVersion >= intArrayOf(4, 3)) {
            reader.readNextFloatArray()
        } else floatArrayOf()
    }
}