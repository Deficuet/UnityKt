package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.ResourceReader
import io.github.deficuet.unitykt.util.compareTo

class AudioClipImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mFormat: Int
    val mType: AudioType
    val m3D: Boolean
    val mUseHardware: Boolean

    val mLoadType: Int
    val mChannels: Int
    val mFrequency: Int
    val mBitsPerSample: Int
    val mLength: Float
    val mIsTrackerFormat: Boolean
    val mSubSoundIndex: Int
    val mPreloadAudioData: Boolean
    val mLoadInBackground: Boolean
    val mLegacy3D: Boolean
    val mCompressionFormat: AudioCompressionFormat

    val mSource: String
    val mOffset: Long
    val mSize: Long
    val mAudioData: ResourceReader

    init {
        if (unityVersion[0] < 5) {
            mFormat = reader.readInt()
            mType = AudioType.of(reader.readInt())
            m3D = reader.readBool()
            mUseHardware = reader.readBool()
            reader.alignStream()
            if (unityVersion >= intArrayOf(3, 2)) {
                reader += 4     //m_Stream: Int
                mSize = reader.readInt().toLong()
                val tSize = if (mSize % 4 != 0L) mSize + 4 - mSize % 4 else mSize
                if (reader.byteSize.toLong() + reader.byteStart - reader.absolutePosition != tSize) {
                    mOffset = reader.readUInt().toLong()
                    mSource = "${asserFile.name}.resS"
                } else {
                    mOffset = 0
                    mSource = ""
                }
            } else {
                mSize = reader.readInt().toLong()
                mOffset = 0
                mSource = ""
            }
            mLoadType = 0
            mChannels = 0
            mFrequency = 0
            mBitsPerSample = 0
            mLength = 0f
            mIsTrackerFormat = false
            mSubSoundIndex = 0
            mPreloadAudioData = false
            mLoadInBackground = false
            mLegacy3D = false
            mCompressionFormat = AudioCompressionFormat.UNKNOWN
        } else {
            mLoadType = reader.readInt()
            mChannels = reader.readInt()
            mFrequency = reader.readInt()
            mBitsPerSample = reader.readInt()
            mLength  = reader.readFloat()
            mIsTrackerFormat = reader.readBool()
            reader.alignStream()
            mSubSoundIndex = reader.readInt()
            mPreloadAudioData = reader.readBool()
            mLoadInBackground = reader.readBool()
            mLegacy3D = reader.readBool()
            reader.alignStream()
            mSource = reader.readAlignedString()
            mOffset = reader.readLong()
            mSize = reader.readLong()
            mCompressionFormat = AudioCompressionFormat.of(reader.readInt())
            mFormat = 0
            mType = AudioType.UNKNOWN
            m3D = false
            mUseHardware = false
        }
        mAudioData = if (mSource.isNotEmpty()) {
            ResourceReader(mSource, asserFile, mOffset, mSize)
        } else {
            ResourceReader(reader, reader.absolutePosition, mSize)
        }
    }
}

enum class AudioType(val id: Int, val ext: String) {
    UNKNOWN(1, "."),
    ACC(2, ".m4a"),
    AIFF(3, ".aif"),
    IT(10, ".it"),
    MOD(12, ".mod"),
    MPEG(13, ".mp3"),
    OGGVORBIS(14, ".ogg"),
    S3M(17, ".s3m"),
    WAV(20, ".wav"),
    XM(21, ".xm"),
    XMA(22, ".wav"),
    VAG(23, ".vag"),
    AUDIOQUEUE(24, ".fsb");

    companion object {
        fun of(value: Int): AudioType {
            return values().firstOrNull { it.id == value } ?: UNKNOWN
        }
    }
}

enum class AudioCompressionFormat(val id: Int, val ext: String) {
    PCM(0, ".fsb"),
    Vorbis(1, ".fsb"),
    ADPCM(2, ".fsb"),
    MP3(3, ".fsb"),
    VAG(4, ".vag"),
    HEVAG(5, ".vag"),
    XMA(6, ".wav"),
    AAC(7, ".m4a"),
    GCADPCM(8, ".fsb"),
    ATRAC9(9, ".at9"),
    UNKNOWN(10, ".");

    companion object {
        fun of(value: Int): AudioCompressionFormat {
            return values().firstOrNull { it.id == value } ?: UNKNOWN
        }
    }
}