package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AnimationType;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AnimationClipFields extends NamedObjectImpl {
    AnimationClipFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }
    
    AnimationType fmAnimationType;
    Boolean fmLegacy;
    Boolean fmCompressed;
    Boolean fmUseHighQualityCurve;
    QuaternionCurveImpl[] fmRotationCurves;
    CompressedAnimationCurveImpl[] fmCompressedRotationCurves;
    Vector3CurveImpl[] fmEulerCurves;
    Vector3CurveImpl[] fmPositionCurves;
    Vector3CurveImpl[] fmScaleCurves;
    FloatCurveImpl[] fmFloatCurves;
    PPtrCurveImpl[] fmPPtrCurves;
    Float fmSampleRate;
    Integer fmWrapMode;
    @Nullable AABBImpl fmBounds;
    UInt fmMuscleClipSize;
    @Nullable ClipMuscleConstantImpl fmMuscleClip;
    @Nullable AnimationClipBindingConstantImpl fmClipBindingConstant;
    AnimationEventImpl[] fmEvents;
}
