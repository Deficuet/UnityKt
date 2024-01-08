package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.Transform;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.math.Quaternion;
import io.github.deficuet.unitykt.math.Vector3;
import io.github.deficuet.unitykt.pptr.PPtr;
import org.jetbrains.annotations.NotNull;

abstract class TransformFields extends ComponentImpl {
    TransformFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Quaternion fmLocalRotation;
    Vector3 fmLocalPosition;
    Vector3 fmLocalScale;
    PPtr<Transform>[] fmChildren;
    PPtr<Transform> fmFather;
}
