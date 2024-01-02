package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AudioCompressionFormat;
import io.github.deficuet.unitykt.classes.FMODSoundType;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.util.ResourceReader;
import org.jetbrains.annotations.NotNull;

abstract class AudioClipFields extends NamedObjectImpl {
    AudioClipFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }
    
    Integer fmFormat;
    FMODSoundType fmType;
    Boolean fm3D;
    Boolean fmUseHardware;
    Integer fmLoadType;
    Integer fmChannels;
    Integer fmFrequency;
    Integer fmBitsPerSample;
    Float fmLength;
    Boolean fmIsTrackerFormat;
    Integer fmSubSoundIndex;
    Boolean fmPreloadAudioData;
    Boolean fmLoadInBackground;
    Boolean fmLegacy3D;
    AudioCompressionFormat fmCompressionFormat;
    String fmSource;
    Long fmOffset;
    Long fmSize;
    ResourceReader pfAudioData;
}
