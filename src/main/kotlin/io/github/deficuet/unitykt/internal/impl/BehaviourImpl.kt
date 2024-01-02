package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.Behaviour
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal abstract class BehaviourImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Behaviour, BehaviourFields(assetFile, info) {
    final override val mEnabled: Byte get() {
        checkInitialize()
        return fmEnabled
    }

    override fun read() {
        super.read()
        fmEnabled = reader.readInt8()
        reader.alignStream()
    }
}