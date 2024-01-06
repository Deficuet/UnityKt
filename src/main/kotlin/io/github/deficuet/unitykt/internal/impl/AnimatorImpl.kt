package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Animator
import io.github.deficuet.unitykt.classes.Avatar
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.classes.RuntimeAnimatorController
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.compareTo

internal class AnimatorImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Animator, AnimatorFields(assetFile, info) {
    override val mAvatar: PPtr<Avatar> get() {
        checkInitialize()
        return fmAvatar
    }
    override val mController: PPtr<RuntimeAnimatorController> get() {
        checkInitialize()
        return fmController
    }
    override val mHasTransformHierarchy: Boolean get() {
        checkInitialize()
        return fmHasTransformHierarchy
    }

    override fun read() {
        super.read()
        fmAvatar = PPtrImpl(reader)
        fmController = PPtrImpl(reader)
        reader.skip(4)     //m_CullingMode: Int
        val v45 = intArrayOf(4, 5)
        if (unityVersion >= v45) reader.skip(4)    //m_UpdateMode: Int
        reader.skip(1)     //m_ApplyRootMotion: Boolean
        if (v45 <= unityVersion && unityVersion[0] < 5) reader.alignStream()
        if (unityVersion[0] >= 5) {
            reader.skip(1)     //m_LinearVelocityBlending: Boolean
            if (unityVersion >= intArrayOf(2021, 2)) reader.skip(1)    //m_StabilizeFeet: Boolean
            reader.alignStream()
        }
        if (unityVersion < v45) reader.skip(1)     //m_AnimatePhysics: Boolean
        fmHasTransformHierarchy = if (unityVersion >= intArrayOf(4, 3)) reader.readBool() else true
        if (unityVersion >= v45) reader.skip(1)    //m_AllowConstantClipSamplingOptimization: Boolean
        if (unityVersion[0] in 5..2017) reader.alignStream()
        if (unityVersion[0] >= 2018) {
            reader.skip(1)     //m_KeepAnimatorControllerStateOnDisable: Boolean
            reader.alignStream()
        }
    }
}