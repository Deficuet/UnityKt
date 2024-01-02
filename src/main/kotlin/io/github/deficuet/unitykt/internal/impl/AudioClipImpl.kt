package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.AudioClip
import io.github.deficuet.unitykt.classes.AudioCompressionFormat
import io.github.deficuet.unitykt.classes.FMODSoundType
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.ResourceReader
import io.github.deficuet.unitykt.util.compareTo

internal class AudioClipImpl(
    assetFile: SerializedFile, info: ObjectInfo
): AudioClip, AudioClipFields(assetFile, info) {
    override val mFormat: Int get() {
        checkInitialize()
        return fmFormat
    }
    override val mType: FMODSoundType get() {
        checkInitialize()
        return fmType
    }
    override val m3D: Boolean get() {
        checkInitialize()
        return fm3D
    }
    override val mUseHardware: Boolean get() {
        checkInitialize()
        return fmUseHardware
    }
    override val mLoadType: Int get() {
        checkInitialize()
        return fmLoadType
    }
    override val mChannels: Int get() {
        checkInitialize()
        return fmChannels
    }
    override val mFrequency: Int get() {
        checkInitialize()
        return fmFrequency
    }
    override val mBitsPerSample: Int get() {
        checkInitialize()
        return fmBitsPerSample
    }
    override val mLength: Float get() {
        checkInitialize()
        return fmLength
    }
    override val mIsTrackerFormat: Boolean get() {
        checkInitialize()
        return fmIsTrackerFormat
    }
    override val mSubSoundIndex: Int get() {
        checkInitialize()
        return fmSubSoundIndex
    }
    override val mPreloadAudioData: Boolean get() {
        checkInitialize()
        return fmPreloadAudioData
    }
    override val mLoadInBackground: Boolean get() {
        checkInitialize()
        return fmLoadInBackground
    }
    override val mLegacy3D: Boolean get() {
        checkInitialize()
        return fmLegacy3D
    }
    override val mCompressionFormat: AudioCompressionFormat get() {
        checkInitialize()
        return fmCompressionFormat
    }
    override val mSource: String get() {
        checkInitialize()
        return fmSource
    }
    override val mOffset: Long get() {
        checkInitialize()
        return fmOffset
    }
    override val mSize: Long get() {
        checkInitialize()
        return fmSize
    }

    private val audioData: ResourceReader get() {
        checkInitialize()
        return pfAudioData
    }

    override fun read() {
        super.read()
        if (unityVersion[0] < 5) {
            fmFormat = reader.readInt32()
            fmType = FMODSoundType.of(reader.readInt32())
            fm3D = reader.readBool()
            fmUseHardware = reader.readBool()
            reader.alignStream()
            if (unityVersion >= intArrayOf(3, 2)) {
                reader.skip(4)     //m_Stream: Int
                fmSize = reader.readInt32().toLong()
                val tSize = if (fmSize % 4 != 0L) fmSize + 4 - fmSize % 4 else fmSize
                if (reader.info.byteSize.toLong() + reader.info.byteStart - reader.absolutePosition != tSize) {
                    fmOffset = reader.readUInt32().toLong()
                    fmSource = "${assetFile.name}.resS"
                } else {
                    fmOffset = 0
                    fmSource = ""
                }
            } else {
                fmSize = reader.readInt32().toLong()
                fmOffset = 0
                fmSource = ""
            }
            fmLoadType = 0
            fmChannels = 0
            fmFrequency = 0
            fmBitsPerSample = 0
            fmLength = 0f
            fmIsTrackerFormat = false
            fmSubSoundIndex = 0
            fmPreloadAudioData = false
            fmLoadInBackground = false
            fmLegacy3D = false
            fmCompressionFormat = AudioCompressionFormat.UNKNOWN
        } else {
            fmLoadType = reader.readInt32()
            fmChannels = reader.readInt32()
            fmFrequency = reader.readInt32()
            fmBitsPerSample = reader.readInt32()
            fmLength  = reader.readFloat()
            fmIsTrackerFormat = reader.readBool()
            reader.alignStream()
            fmSubSoundIndex = reader.readInt32()
            fmPreloadAudioData = reader.readBool()
            fmLoadInBackground = reader.readBool()
            fmLegacy3D = reader.readBool()
            reader.alignStream()
            fmSource = reader.readAlignedString()
            fmOffset = reader.readInt64()
            fmSize = reader.readInt64()
            fmCompressionFormat = AudioCompressionFormat.of(reader.readInt32())
            fmFormat = 0
            fmType = FMODSoundType.UNKNOWN
            fm3D = false
            fmUseHardware = false
        }
        pfAudioData = if (fmSource.isNotEmpty()) {
            ResourceReader(fmSource, assetFile, fmOffset, fmSize)
        } else {
            ResourceReader(reader, reader.absolutePosition, fmSize)
        }.registerToManager(assetFile.root.manager)
    }

    override fun getRawData(): ByteArray {
        return audioData.read()
    }
}