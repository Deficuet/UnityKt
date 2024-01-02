package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class GameObjectImpl(
    assetFile: SerializedFile, info: ObjectInfo
): GameObject, GameObjectFields(assetFile, info) {
    override val mComponents: Array<out PPtr<Component>> get() {
        checkInitialize()
        return fmComponents
    }
    override val mLayer: UInt get() {
        checkInitialize()
        return fmLayer
    }
    override val mName: String get() {
        checkInitialize()
        return fmName
    }
    override val mTag: UShort get() {
        checkInitialize()
        return fmTag
    }
    override val mIsActive: Boolean get() {
        checkInitialize()
        return fmIsActive
    }
    override val mTransform: Transform get() {
        checkInitialize()
        return fmTransform
    }
    override val mMeshRenderer: MeshRenderer get() {
        checkInitialize()
        return fmMeshRenderer
    }
    override val mMeshFilter: MeshFilter get() {
        checkInitialize()
        return fmMeshFilter
    }
    override val mSkinnedMeshRenderer: SkinnedMeshRenderer get() {
        checkInitialize()
        return fmSkinnedMeshRenderer
    }
    override val mAnimator: Animator get() {
        checkInitialize()
        return fmAnimator
    }
    override val mAnimation: Animation get() {
        checkInitialize()
        return fmAnimation
    }

    override fun read() {
        super.read()
        fmComponents = reader.readArrayOf {
            if (unityVersion < intArrayOf(5, 5)) {
                skip(4)     //first: Int
            }
            val p = PPtrImpl<Component>(this)
            val obj = p.safeGetObj()
            if (obj != null) {
                when (obj) {
                    is Transform -> fmTransform = obj
                    is MeshRenderer -> fmMeshRenderer = obj
                    is MeshFilter -> fmMeshFilter = obj
                    is SkinnedMeshRenderer -> fmSkinnedMeshRenderer = obj
                    is Animator -> fmAnimator = obj
                    is Animation -> fmAnimation = obj
                }
            }
            p
        }
        reader.alignStream()
        fmLayer = reader.readUInt32()
        fmName = reader.readAlignedString()
        fmTag = reader.readUInt16()
        fmIsActive = reader.readBool()
    }
}