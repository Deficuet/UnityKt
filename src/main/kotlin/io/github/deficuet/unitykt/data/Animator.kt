package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class Animator internal constructor(reader: ObjectReader): Behaviour(reader) {
    val mAvatar = PPtr<Avatar>(reader)
    val mController = PPtr<RuntimeAnimatorController>(reader)
    val mHasTransformHierarchy: Boolean

    init {
        reader += 4     //m_CullingMode: Int
        val v45 = intArrayOf(4, 5)
        if (unityVersion >= v45) reader += 4    //m_UpdateMode: Int
        reader += 1     //m_ApplyRootMotion: Boolean
        if (v45 <= unityVersion && unityVersion[0] < 5) reader.alignStream()
        if (unityVersion[0] >= 5) {
            reader += 1     //m_LinearVelocityBlending: Boolean
            if (unityVersion >= intArrayOf(2021, 2)) reader += 1    //m_StabilizeFeet: Boolean
            reader.alignStream()
        }
        if (unityVersion < v45) reader += 1     //m_AnimatePhysics: Boolean
        mHasTransformHierarchy = if (unityVersion >= intArrayOf(4, 3)) reader.readBool() else true
        if (unityVersion >= v45) reader += 1    //m_AllowConstantClipSamplingOptimization: Boolean
        if (unityVersion[0] in 5..2017) reader.alignStream()
        if (unityVersion[0] >= 2018) {
            reader += 1     //m_KeepAnimatorControllerStateOnDisable: Boolean
            reader.alignStream()
        }
    }
}