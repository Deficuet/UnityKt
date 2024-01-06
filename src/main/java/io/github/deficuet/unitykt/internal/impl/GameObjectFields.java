package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.*;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import kotlin.UInt;
import kotlin.UShort;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class GameObjectFields extends EditorExtensionImpl {
    GameObjectFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<Component>[] fmComponents;
    UInt fmLayer;
    String fmName;
    UShort fmTag;
    Boolean fmIsActive;

    @Nullable Transform fmTransform = null;
    @Nullable MeshRenderer fmMeshRenderer = null;
    @Nullable MeshFilter fmMeshFilter = null;
    @Nullable SkinnedMeshRenderer fmSkinnedMeshRenderer = null;
    @Nullable Animator fmAnimator = null;
    @Nullable Animation fmAnimation = null;
}
