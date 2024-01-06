package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface MovieTexture: Texture {
    val mLoop: Boolean
    val mAudioClip: PPtr<AudioClip>
    val mMovieData: ByteArray
}