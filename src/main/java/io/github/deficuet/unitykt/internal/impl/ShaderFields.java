package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.ShaderCompilerPlatform;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class ShaderFields extends NamedObjectImpl {
    ShaderFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    byte[] fmScript;
    UInt fDecompressedSize;
    byte[] fmSubProgramBlob;
    @Nullable SerializedShaderImpl fmParsedForm;
    ShaderCompilerPlatform[] fPlatforms;
    UInt[][] fOffsets;
    UInt[][] fCompressedLengths;
    UInt[][] fDecompressedLengths;
    byte[] fCompressedBlob;
}
