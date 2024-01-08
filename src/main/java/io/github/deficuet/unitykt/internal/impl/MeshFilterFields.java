package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.Mesh;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import org.jetbrains.annotations.NotNull;

abstract class MeshFilterFields extends ComponentImpl{
    MeshFilterFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<Mesh> fmMesh;
}
