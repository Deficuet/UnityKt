package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.EditorExtension
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal abstract class EditorExtensionImpl(
    assetFile: SerializedFile, info: ObjectInfo
): EditorExtension, EditorExtensionFields(assetFile, info) {
    override fun read() {
        super.read()
        if (platform == BuildTarget.NoTarget) {
            PPtrImpl<EditorExtension>(reader)
            PPtrImpl<UnityObject>(reader)
        }
    }
}