package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.MeshRenderer
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal class MeshRendererImpl(
    assetFile: SerializedFile, info: ObjectInfo
): MeshRenderer, MeshRendererFields(assetFile, info)