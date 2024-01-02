package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.enums.NumericalEnum
import io.github.deficuet.unitykt.enums.NumericalEnumCompanion

interface AudioClip: NamedObject {
    val mFormat: Int
    val mType: FMODSoundType
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

    fun getRawData(): ByteArray
}

enum class FMODSoundType(override val id: Int, val ext: String): NumericalEnum<Int> {
    UNKNOWN(1, ""),
    ACC(2, ".m4a"),
    AIFF(3, ".aif"),
    ASF(3, ""),
    AT3(4, ""),
    CDDA(5, ""),
    DLS(6, ""),
    FLAC(7, ""),
    FSB(8, ""),
    GCADPCM(9, ""),
    IT(10, ".it"),
    MIDI(11, ""),
    MOD(12, ".mod"),
    MPEG(13, ".mp3"),
    OGGVORBIS(14, ".ogg"),
    PLAYLIST(15, ""),
    RAW(16, ""),
    S3M(17, ".s3m"),
    SF2(18, ""),
    USER(19, ""),
    WAV(20, ".wav"),
    XM(21, ".xm"),
    XMA(22, ".wav"),
    VAG(23, ".vag"),
    AUDIOQUEUE(24, ".fsb"),
    XWMA(25, ""),
    BCWAV(26, ""),
    AT9(27, ""),
    VORBIS(28, ""),
    MEDIA_FOUNDATION(29, "");

    companion object: NumericalEnumCompanion<Int, FMODSoundType>(values(), UNKNOWN)
}


enum class AudioCompressionFormat(override val id: Int, val ext: String): NumericalEnum<Int> {
    PCM(0, ".fsb"),
    Vorbis(1, ".fsb"),
    ADPCM(2, ".fsb"),
    MP3(3, ".fsb"),
    PSVAG(4, ".fsb"),
    HEVAG(5, ".fsb"),
    XMA(6, ".fsb"),
    AAC(7, ".m4a"),
    GCADPCM(8, ".fsb"),
    ATRAC9(9, ".fsb"),
    UNKNOWN(9999, ".");

    companion object: NumericalEnumCompanion<Int, AudioCompressionFormat>(values(), UNKNOWN)
}
