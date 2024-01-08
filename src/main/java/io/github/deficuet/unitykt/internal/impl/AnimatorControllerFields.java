package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AnimationClip;
import io.github.deficuet.unitykt.classes.ControllerConstant;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

abstract class AnimatorControllerFields extends RuntimeAnimatorControllerImpl {
    AnimatorControllerFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    UInt fmControllerSize;
    ControllerConstant fmController;
    Map<UInt, List<String>> fmTOS;
    PPtr<AnimationClip>[] fmAnimationClip;
}
