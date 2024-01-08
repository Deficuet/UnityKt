package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AudioClip;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import io.github.deficuet.unitykt.pptr.PPtr;
import org.jetbrains.annotations.NotNull;

abstract class MovieTextureFields extends TextureImpl {
    MovieTextureFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    Boolean fmLoop;
    PPtr<AudioClip> fmAudioClip;
    byte[] fmMovieData;
}
