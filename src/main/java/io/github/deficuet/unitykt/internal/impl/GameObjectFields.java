package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.*;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import kotlin.UInt;
import kotlin.UShort;
import org.jetbrains.annotations.NotNull;

abstract class GameObjectFields extends EditorExtensionImpl {
    GameObjectFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<Component>[] fmComponents;
    UInt fmLayer;
    String fmName;
    UShort fmTag;
    Boolean fmIsActive;
    Transform fmTransform;
    MeshRenderer fmMeshRenderer;
    MeshFilter fmMeshFilter;
    SkinnedMeshRenderer fmSkinnedMeshRenderer;
    Animator fmAnimator;
    Animation fmAnimation;
}
