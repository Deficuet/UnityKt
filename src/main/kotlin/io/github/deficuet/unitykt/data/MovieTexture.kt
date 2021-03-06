package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.MovieTextureImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class MovieTexture private constructor(
    private val container: ImplementationContainer<MovieTextureImpl>
): Texture(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { MovieTextureImpl(ObjectReader(assetFile, info)) })

    val mMovieData: ByteArray           get() = container.impl.mMovieData
    val mAudioClip: PPtr<AudioClip>     get() = container.impl.mAudioClip
}