package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AudioClipImpl
import io.github.deficuet.unitykt.dataImpl.AudioCompressionFormat
import io.github.deficuet.unitykt.dataImpl.FMODSoundType
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.ResourceReader

class AudioClip private constructor(
    private val container: ImplementationContainer<AudioClipImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AudioClipImpl(ObjectReader(assetFile, info)) })

    val mFormat: Int                                    get() = container.impl.mFormat
    val mType: FMODSoundType                                get() = container.impl.mType
    val m3D: Boolean                                    get() = container.impl.m3D
    val mUseHardware: Boolean                           get() = container.impl.mUseHardware

    val mLoadType: Int                                  get() = container.impl.mLoadType
    val mChannels: Int                                  get() = container.impl.mChannels
    val mFrequency: Int                                 get() = container.impl.mFrequency
    val mBitsPerSample: Int                             get() = container.impl.mBitsPerSample
    val mLength: Float                                  get() = container.impl.mLength
    val mIsTrackerFormat: Boolean                       get() = container.impl.mIsTrackerFormat
    val mSubSoundIndex: Int                             get() = container.impl.mSubSoundIndex
    val mPreloadAudioData: Boolean                      get() = container.impl.mPreloadAudioData
    val mLoadInBackground: Boolean                      get() = container.impl.mLoadInBackground
    val mLegacy3D: Boolean                              get() = container.impl.mLegacy3D
    val mCompressionFormat: AudioCompressionFormat      get() = container.impl.mCompressionFormat

    val mSource: String                                 get() = container.impl.mSource
    val mOffset: Long                                   get() = container.impl.mOffset
    val mSize: Long                                     get() = container.impl.mSize
    val mAudioData: ResourceReader                      get() = container.impl.mAudioData
}