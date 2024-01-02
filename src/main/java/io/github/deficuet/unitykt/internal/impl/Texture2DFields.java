package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.GLTextureSettings;
import io.github.deficuet.unitykt.classes.StreamingInfo;
import io.github.deficuet.unitykt.classes.TextureFormat;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.util.ResourceReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class Texture2DFields extends TextureImpl {
    Texture2DFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Integer fmWidth;
    Integer fmHeight;
    TextureFormat fmTextureFormat;
    Boolean fmMipMap;
    Integer fmMipCount;
    GLTextureSettings fmTextureSettings;
    @Nullable StreamingInfo fmStreamData;
    ResourceReader pfImageData;
}
