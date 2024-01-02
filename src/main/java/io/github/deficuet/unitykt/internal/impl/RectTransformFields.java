package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.math.Vector2;
import org.jetbrains.annotations.NotNull;

abstract class RectTransformFields extends TransformImpl {
    RectTransformFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Vector2 fmAnchorMin;
    Vector2 fmAnchorMax;
    Vector2 fmAnchoredPosition;
    Vector2 fmSizeDelta;
    Vector2 fmPivot;
}
