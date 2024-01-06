package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface VideoClip: NamedObject {
    val mOriginalPath: String
    val mProxyWidth: UInt
    val mProxyHeight: UInt
    val mWidth: UInt
    val mHeight: UInt
    val mPixelAspectRatioNumerator: UInt
    val mPixelAspectRatioDenominator: UInt
    val mFrameRate: Double
    val mFrameCount: ULong
    val mFormat: Int
    val mAudioChannelCount: Array<out UShort>
    val mAudioSampleRate: Array<out UInt>
    val mAudioLanguage: Array<out String>
    val mVideoShaders: Array<out PPtr<Shader>>
    val mExternalResource: StreamedResource
    val mHasSplitAlpha: Boolean
    val msRGB: Boolean

    fun getRawData(): ByteArray
}

interface StreamedResource {
    val mSource: String
    val mOffset: Long
    val mSize: Long
}
