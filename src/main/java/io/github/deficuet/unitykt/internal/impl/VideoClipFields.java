package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.PPtr;
import io.github.deficuet.unitykt.classes.Shader;
import io.github.deficuet.unitykt.classes.StreamedResource;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.util.ResourceReader;
import kotlin.UInt;
import kotlin.ULong;
import kotlin.UShort;
import org.jetbrains.annotations.NotNull;

abstract class VideoClipFields extends NamedObjectImpl {
    VideoClipFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    String fmOriginalPath;
    UInt fmProxyWidth;
    UInt fmProxyHeight;
    UInt fmWidth;
    UInt fmHeight;
    UInt fmPixelAspectRatioNumerator;
    UInt fmPixelAspectRatioDenominator;
    Double fmFrameRate;
    ULong fmFrameCount;
    Integer fmFormat;
    UShort[] fmAudioChannelCount;
    UInt[] fmAudioSampleRate;
    String[] fmAudioLanguage;
    PPtr<Shader>[] fmVideoShaders;
    StreamedResource fmExternalResource;
    Boolean fmHasSplitAlpha;
    Boolean fmsRGB;
    ResourceReader pfVideoData;
}
