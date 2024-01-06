package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.AudioClip
import io.github.deficuet.unitykt.classes.MovieTexture
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile

internal class MovieTextureImpl(
    assetFile: SerializedFile, info: ObjectInfo
): MovieTexture, MovieTextureFields(assetFile, info) {
    override val mLoop: Boolean get() {
        checkInitialize()
        return fmLoop
    }
    override val mAudioClip: PPtr<AudioClip> get() {
        checkInitialize()
        return fmAudioClip
    }
    override val mMovieData: ByteArray get() {
        checkInitialize()
        return fmMovieData
    }

    override fun read() {
        super.read()
        fmLoop = reader.readBool()
        reader.alignStream()
        fmAudioClip = PPtrImpl(reader)
        fmMovieData = reader.readInt8Array()
    }
}