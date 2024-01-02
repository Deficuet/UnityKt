package io.github.deficuet.unitykt.classes

interface MovieTexture: Texture {
    val mLoop: Boolean
    val mAudioClip: PPtr<AudioClip>
    val mMovieData: ByteArray
}