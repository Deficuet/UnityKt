package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.MonoScript;
import io.github.deficuet.unitykt.pptr.PPtr;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import org.jetbrains.annotations.NotNull;

abstract class MonoBehaviourFields extends BehaviourImpl {
    MonoBehaviourFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<MonoScript> fmScript;
    String fmName;
}
