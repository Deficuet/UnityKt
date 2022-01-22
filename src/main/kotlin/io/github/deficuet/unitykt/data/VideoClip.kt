package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.VideoClipImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class VideoClip private constructor(
    private val container: ImplementationContainer<VideoClipImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { VideoClipImpl(ObjectReader(assetFile, info)) })

    val mOriginalPath get() = container.impl.mOriginalPath
    val mVideoData get() = container.impl.mVideoData
    val mExternamResource get() = container.impl.mExternamResource
}