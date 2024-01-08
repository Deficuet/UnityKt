package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.Avatar;
import io.github.deficuet.unitykt.classes.RuntimeAnimatorController;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import org.jetbrains.annotations.NotNull;

abstract class AnimatorFields extends BehaviourImpl {
    AnimatorFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<Avatar> fmAvatar;
    PPtr<RuntimeAnimatorController> fmController;
    Boolean fmHasTransformHierarchy;
}
