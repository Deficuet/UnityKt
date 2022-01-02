package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class MovieTexture internal constructor(reader: ObjectReader): Texture(reader) {
    val mMovieData: ByteArray
    val mAudioClip: PPtr<AudioClip>

    init {
        reader += 1     //m_Loop: Boolean
        reader.alignStream()
        mAudioClip = PPtr(reader)
        mMovieData = reader.readNextByteArray()
    }
}