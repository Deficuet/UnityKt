package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Component
import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.classes.PPtr
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal abstract class ComponentImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Component, ComponentFields(assetFile, info) {
    final override val mGameObject: PPtr<GameObject> get() {
        checkInitialize()
        return fmGameObject
    }

    override fun read() {
        super.read()
        fmGameObject = PPtrImpl(reader)
    }
}