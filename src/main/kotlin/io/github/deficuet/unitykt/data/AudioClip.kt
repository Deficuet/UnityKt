package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AudioClipImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AudioClip private constructor(
    private val container: ImplementationContainer<AudioClipImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer { AudioClipImpl(ObjectReader(assetFile, info)) })

    val mFormat get() = container.impl.mFormat
    val mType get() = container.impl.mType
    val m3D get() = container.impl.m3D
    val mUseHardware get() = container.impl.mUseHardware

    val mLoadType get() = container.impl.mLoadType
    val mChannels get() = container.impl.mChannels
    val mFrequency get() = container.impl.mFrequency
    val mBitsPerSample get() = container.impl.mBitsPerSample
    val mLength get() = container.impl.mLength
    val mIsTrackerFormat get() = container.impl.mIsTrackerFormat
    val mSubSoundIndex get() = container.impl.mSubSoundIndex
    val mPreloadAudioData get() = container.impl.mPreloadAudioData
    val mLoadInBackground get() = container.impl.mLoadInBackground
    val mLegacy3D get() = container.impl.mLegacy3D
    val mCompressionFormat get() = container.impl.mCompressionFormat

    val mSource get() = container.impl.mSource
    val mOffset get() = container.impl.mOffset
    val mSize get() = container.impl.mSize
    val mAudioData get() = container.impl.mAudioData
}