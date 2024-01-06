package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.pptr.PPtr;
import io.github.deficuet.unitykt.classes.UnityObject;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

abstract class ResourceManagerFields extends UnityObjectImpl {
    ResourceManagerFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Map<String, List<PPtr<UnityObject>>> fmContainer;
}
