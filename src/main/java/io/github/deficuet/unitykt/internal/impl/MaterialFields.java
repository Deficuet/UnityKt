package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.PPtr;
import io.github.deficuet.unitykt.classes.Shader;
import io.github.deficuet.unitykt.classes.UnityPropertySheet;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import org.jetbrains.annotations.NotNull;

abstract class MaterialFields extends NamedObjectImpl {
    MaterialFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<Shader> fmShader;
    UnityPropertySheet fmSavedProperties;
}
