package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.MonoBehaviour
import io.github.deficuet.unitykt.classes.MonoScript
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal class MonoBehaviourImpl(
    assetFile: SerializedFile, info: ObjectInfo
): MonoBehaviour, MonoBehaviourFields(assetFile, info) {
    override val mScript: PPtr<MonoScript> get() {
        checkInitialize()
        return fmScript
    }
    override val mName: String get() {
        checkInitialize()
        return fmName
    }

    override fun read() {
        super.read()
        fmScript = PPtrImpl(reader)
        fmName = reader.readAlignedString()
    }
}