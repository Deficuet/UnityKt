package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.math.Matrix4x4;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class MeshFields extends NamedObjectImpl {
    MeshFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    SubMashImpl[] fmSubMeshes;
    @Nullable BlendShapeDataImpl fmShapes;
    UInt[] fmIndices;
    Matrix4x4[] fmBindPose = new Matrix4x4[0];
    UInt[] fmBoneNameHashes = new UInt[0];
    int fmVertexCount = 0;
    float[] fmVertices = new float[0];
    BoneWeights4Impl[] fmSkin = new BoneWeights4Impl[0];
    float[] fmNormals = new float[0];
    float[] fmColors = new float[0];
    float[] fmUV0 = new float[0];
    float[] fmUV1 = new float[0];
    float[] fmUV2 = new float[0];
    float[] fmUV3 = new float[0];
    float[] fmUV4 = new float[0];
    float[] fmUV5 = new float[0];
    float[] fmUV6 = new float[0];
    float[] fmUV7 = new float[0];
    float[] fmTangents = new float[0];
}
