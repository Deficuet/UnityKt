package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.AudioClip
import io.github.deficuet.unitykt.util.ObjectReader

class MovieTextureImpl internal constructor(reader: ObjectReader): TextureImpl(reader) {
    val mMovieData: ByteArray
    val mAudioClip: PPtr<AudioClip>

    init {
        reader += 1     //m_Loop: Boolean
        reader.alignStream()
        mAudioClip = PPtr(reader)
        mMovieData = reader.readNextByteArray()
    }
}