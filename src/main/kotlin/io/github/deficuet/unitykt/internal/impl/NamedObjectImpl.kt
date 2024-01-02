package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.NamedObject
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal abstract class NamedObjectImpl(
    assetFile: SerializedFile, info: ObjectInfo
): NamedObject, NamedObjectFields(assetFile, info) {
    final override val mName: String get() {
        checkInitialize()
        return fmName
    }

    override fun read() {
        super.read()
        fmName = reader.readAlignedString()
    }
}