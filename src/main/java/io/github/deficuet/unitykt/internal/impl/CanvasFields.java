package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.UnityObject;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import org.jetbrains.annotations.NotNull;

abstract class CanvasFields extends BehaviourImpl {
    CanvasFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Integer fmRenderMode;
    PPtr<UnityObject> fmCamera;
    Float fmPlaneDistance;
    Boolean fmPixelPerfect;
    Boolean fmReceivesEvent;
    Boolean fmOverrideSorting;
    Boolean fmOverridePixelPerfect;
    Float fmSortingBucketNormalizedSize;
    Integer fmAdditionalShaderChannelsFlag;
    Integer fmSortingLayerID;
    Short fmSortingOrder;
    Byte fmTargetDisplay;
}
