package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AnimationClipOverride;
import io.github.deficuet.unitykt.classes.PPtr;
import io.github.deficuet.unitykt.classes.RuntimeAnimatorController;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import org.jetbrains.annotations.NotNull;

abstract class AnimatorOverrideControllerFields extends RuntimeAnimatorControllerImpl {
    AnimatorOverrideControllerFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<RuntimeAnimatorController> fmController;
    AnimationClipOverride[] fmClips;
}
