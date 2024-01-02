package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.PPtr
import io.github.deficuet.unitykt.classes.Shader
import io.github.deficuet.unitykt.classes.StreamedResource
import io.github.deficuet.unitykt.classes.VideoClip
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.ResourceReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class VideoClipImpl(
    assetFile: SerializedFile, info: ObjectInfo
): VideoClip, VideoClipFields(assetFile, info) {
    override val mOriginalPath: String get() {
        checkInitialize()
        return fmOriginalPath
    }
    override val mProxyWidth: UInt get() {
        checkInitialize()
        return fmProxyWidth
    }
    override val mProxyHeight: UInt get() {
        checkInitialize()
        return fmProxyHeight
    }
    override val mWidth: UInt get() {
        checkInitialize()
        return fmWidth
    }
    override val mHeight: UInt get() {
        checkInitialize()
        return fmHeight
    }
    override val mPixelAspectRatioNumerator: UInt get() {
        checkInitialize()
        return fmPixelAspectRatioNumerator
    }
    override val mPixelAspectRatioDenominator: UInt get() {
        checkInitialize()
        return fmPixelAspectRatioDenominator
    }
    override val mFrameRate: Double get() {
        checkInitialize()
        return fmFrameRate
    }
    override val mFrameCount: ULong get() {
        checkInitialize()
        return fmFrameCount
    }
    override val mFormat: Int get() {
        checkInitialize()
        return fmFormat
    }
    override val mAudioChannelCount: Array<out UShort> get() {
        checkInitialize()
        return fmAudioChannelCount
    }
    override val mAudioSampleRate: Array<out UInt> get() {
        checkInitialize()
        return fmAudioSampleRate
    }
    override val mAudioLanguage: Array<out String> get() {
        checkInitialize()
        return fmAudioLanguage
    }
    override val mVideoShaders: Array<out PPtr<Shader>> get() {
        checkInitialize()
        return fmVideoShaders
    }
    override val mExternalResource: StreamedResource get() {
        checkInitialize()
        return fmExternalResource
    }
    override val mHasSplitAlpha: Boolean get() {
        checkInitialize()
        return fmHasSplitAlpha
    }
    override val msRGB: Boolean get() {
        checkInitialize()
        return fmsRGB
    }

    private val videoData: ResourceReader get() {
        checkInitialize()
        return pfVideoData
    }

    override fun read() {
        super.read()
        fmOriginalPath = reader.readAlignedString()
        fmProxyWidth = reader.readUInt32()
        fmProxyHeight = reader.readUInt32()
        fmWidth = reader.readUInt32()
        fmHeight = reader.readUInt32()
        if (unityVersion >= intArrayOf(2017, 2)) {
            fmPixelAspectRatioNumerator = reader.readUInt32()
            fmPixelAspectRatioDenominator = reader.readUInt32()
        } else {
            fmPixelAspectRatioNumerator = 0u
            fmPixelAspectRatioDenominator = 0u
        }
        fmFrameRate = reader.readDouble()
        fmFrameCount = reader.readUInt64()
        fmFormat = reader.readInt32()
        fmAudioChannelCount = reader.readUInt16Array()
        reader.alignStream()
        fmAudioSampleRate = reader.readUInt32Array()
        fmAudioLanguage = reader.readAlignedStringArray()
        fmVideoShaders = if (unityVersion[0] >= 2020) {
            reader.readArrayOf { PPtrImpl(reader) }
        } else emptyArray()
        fmExternalResource = StreamedResourceImpl(reader)
        fmHasSplitAlpha = reader.readBool()
        fmsRGB = if (unityVersion[0] >= 2020) reader.readBool() else false
        pfVideoData = if (fmExternalResource.mSource.isNotEmpty()) {
            with(fmExternalResource) {
                ResourceReader(mSource, assetFile, mOffset, mSize)
            }
        } else {
            ResourceReader(reader, reader.absolutePosition, fmExternalResource.mSize)
        }.registerToManager(assetFile.root.manager)
    }

    override fun getRawData(): ByteArray {
        return videoData.read()
    }
}

internal class StreamedResourceImpl(reader: ObjectReader): StreamedResource {
    override val mSource = reader.readAlignedString()
    override val mOffset = reader.readInt64()
    override val mSize = reader.readInt64()
}
