package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import org.jetbrains.annotations.NotNull;

abstract class MonoScriptFields extends NamedObjectImpl {
    MonoScriptFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    String fmClassName;
    String fmNameSpace;
    String fmAssemblyName;
}