package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.RuntimeAnimatorController
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal abstract class RuntimeAnimatorControllerImpl(
    assetFile: SerializedFile, info: ObjectInfo
): RuntimeAnimatorController, RuntimeAnimatorControllerFields(assetFile, info)