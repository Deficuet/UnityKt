package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.classes.ResourceManager
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.readArrayOf

internal class ResourceManagerImpl(
    assetFile: SerializedFile, info: ObjectInfo
): ResourceManager, ResourceManagerFields(assetFile, info) {
    override val mContainer: Map<String, List<PPtr<UnityObject>>> get() {
        checkInitialize()
        return fmContainer
    }

    override fun read() {
        super.read()
        fmContainer = reader.readArrayOf { readAlignedString() to PPtrImpl<UnityObject>(reader) }
            .groupBy({ it.first }, { it.second })
    }
}