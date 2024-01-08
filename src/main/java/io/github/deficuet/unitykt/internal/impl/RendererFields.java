package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.Material;
import io.github.deficuet.unitykt.classes.StaticBatchInfo;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;

abstract class RendererFields extends ComponentImpl {
    RendererFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<Material>[] fmMaterials;
    StaticBatchInfo fmStaticBatchInfo;
    UInt[] fmSubsetIndices;
}
