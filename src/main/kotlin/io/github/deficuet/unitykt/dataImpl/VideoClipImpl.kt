package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.ResourceReader
import io.github.deficuet.unitykt.util.compareTo

class VideoClipImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mOriginalPath = reader.readAlignedString()
    val mVideoData: ResourceReader
    val mExternamResource: StreamedResource

    init {
        reader += 32    //m_ProxyWidth, m_ProxyHeight, Width, Height: UInt
        if (unityVersion >= intArrayOf(2017, 2)) {
            reader += 8     //m_PixelAspecRatioNum, m_PixelAspecRatioDen: UInt
        }
        reader += 20    //m_FrameRate: Double, m_FrameCount: ULong, m_Format: Int
        reader.readNextUShortArray()    //m_AudioChannelCount
        reader.alignStream()
        reader.readNextUIntArray()      //m_AudioSampleRate
        reader.readNextStringArray()    //m_AudioLanguage
        if (unityVersion[0] >= 2020) {
            reader.readArrayOf { PPtr<ShaderImpl>(reader) }     //m_VideoShaders
        }
        mExternamResource = StreamedResource(reader)
        reader += 1     //m_HasSplitAlpha: Boolean
        if (unityVersion[0] >= 2020) reader += 1    //m_sRGB
        mVideoData = if (mExternamResource.mSource.isNotEmpty()) {
            with(mExternamResource) {
                ResourceReader(mSource, asserFile, mOffset, mSize)
            }
        } else {
            ResourceReader(reader, reader.absolutePosition, mExternamResource.mSize)
        }
    }
}

class StreamedResource internal constructor(reader: ObjectReader) {
    val mSource = reader.readAlignedString()
    val mOffset = reader.readLong()
    val mSize = reader.readLong()
}