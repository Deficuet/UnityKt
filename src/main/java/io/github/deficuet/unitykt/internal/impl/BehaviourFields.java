package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import org.jetbrains.annotations.NotNull;

abstract class BehaviourFields extends ComponentImpl {
    BehaviourFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Byte fmEnabled;
}
