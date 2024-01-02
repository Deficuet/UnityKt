package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.GLTextureSettings
import io.github.deficuet.unitykt.classes.StreamingInfo
import io.github.deficuet.unitykt.classes.Texture2D
import io.github.deficuet.unitykt.classes.TextureFormat
import io.github.deficuet.unitykt.enums.BuildTarget
import io.github.deficuet.unitykt.extension.TextureDecoder
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import java.awt.image.*
import kotlin.math.pow
import kotlin.math.roundToInt

internal class Texture2DImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Texture2D, Texture2DFields(assetFile, info) {
    override val mWidth: Int get() {
        checkInitialize()
        return fmWidth
    }
    override val mHeight: Int get() {
        checkInitialize()
        return fmHeight
    }
    override val mTextureFormat: TextureFormat get() {
        checkInitialize()
        return fmTextureFormat
    }
    override val mMipMap: Boolean get() {
        checkInitialize()
        return fmMipMap
    }
    override val mMipCount: Int get() {
        checkInitialize()
        return fmMipCount
    }
    override val mTextureSettings: GLTextureSettings get() {
        checkInitialize()
        return fmTextureSettings
    }
    override val mStreamData: StreamingInfo? get() {
        checkInitialize()
        return fmStreamData
    }
    private val imageData: ResourceReader get() {
        checkInitialize()
        return pfImageData
    }

    override fun read() {
        super.read()
        val version = reader.unityVersion
        fmWidth = reader.readInt32()
        fmHeight = reader.readInt32()
        reader.skip(4)     //m_CompleteImageSize: Int
        if (version[0] >= 2020) reader.skip(4)     //m_MipsStripped: Int
        fmTextureFormat = TextureFormat.of(reader.readInt32())
        if (version < intArrayOf(5, 2)) {
            fmMipMap = reader.readBool()
            fmMipCount = 0
        } else {
            fmMipCount = reader.readInt32()
            fmMipMap = false
        }
        if (version >= intArrayOf(2, 6)) reader.skip(1)        //m_IsReadable: Boolean
        if (version[0] >= 2020) reader.skip(1)     //m_IsPreProcessed: Boolean
        if (version >= intArrayOf(2019, 3)) reader.skip(1)     //m_IgnoreMasterTextureLimit: Boolean
        if (version[0] >= 3 && version <= intArrayOf(5, 4)) reader.skip(1)      //m_ReadAllowed: Boolean
        if (version >= intArrayOf(2018, 2)) reader.skip(1)     //m_StreamingMipmaps: Boolean
        reader.alignStream()
        if (version >= intArrayOf(2018, 2)) reader.skip(4)     //m_StreamingMipmapsPriority: Int
        reader.skip(8)     //m_ImageCount, m_TextureDimension: Int
        fmTextureSettings = GLTextureSettingsImpl(reader)
        if (version[0] >= 3) reader.skip(4)    //m_LightmapFormat: Int
        if (version >= intArrayOf(3, 5)) reader.skip(4)    //m_ColorSpace: Int
        if (version >= intArrayOf(2020, 2)) {
            reader.readInt8Array()      //m_PlatformBlob
            reader.alignStream()
        }
        val imageDataSize = reader.readInt32()
        fmStreamData = if (imageDataSize == 0 && version >= intArrayOf(5, 3)) {
            StreamingInfoImpl(reader)
        } else null
        pfImageData = if (fmStreamData?.path?.isNotEmpty() == true) {
            ResourceReader(fmStreamData!!.path, assetFile, fmStreamData!!.offset, fmStreamData!!.size.toLong())
        } else {
            ResourceReader(reader, reader.absolutePosition, imageDataSize.toLong())
        }.registerToManager(assetFile.root.manager)
    }

    override fun getDecompressedData(): ByteArray? {
        return decompressTexture(imageData.read())
    }

    override fun getImage(): BufferedImage? {
        return getDecompressedData()?.let { raw ->
            BufferedImage(mWidth, mHeight, BufferedImage.TYPE_4BYTE_ABGR).apply {
                data = Raster.createRaster(
                    ComponentSampleModel(
                        DataBuffer.TYPE_BYTE, mWidth, mHeight, 4,
                        mWidth * 4, intArrayOf(2, 1, 0, 3)
                    ),
                    DataBufferByte(raw, raw.size), null
                )
            }
        }
    }

    private fun swapForXBOX360(b: ByteArray) {
        if (platform == BuildTarget.XBOX360) b.reverse()
    }

    private fun decompressTexture(data: ByteArray): ByteArray? {
        val areaIndices = 0 until mWidth * mHeight
        val dataSize = mWidth * mHeight * 4
        val dataSizeIndices = 0 until dataSize step 4
        val out: ByteArray?
        when (mTextureFormat) {
            TextureFormat.Alpha8 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        3 -> data[it / 4]
                        else -> -1
                    }
                }
            }
            TextureFormat.ARGB4444 -> {
                swapForXBOX360(data)
                out = ByteArray(dataSize) { idx ->
                    when (idx % 4) {
                        0 -> data[(idx / 4 * 2 + 1).toUInt()].and(0x0F)
                                .let { it.shl(4).or(it) }.toByte()
                        1 -> data[(idx / 4 * 2 + 1).toUInt()].and(0xF0).shr(4)
                                .let { it.shl(4).or(it) }.toByte()
                        2 -> data[(idx / 4 * 2).toUInt()].and(0x0F)
                                .let { it.shl(4).or(it) }.toByte()
                        else -> data[(idx / 4 * 2).toUInt()].and(0xF0).shr(4)
                                .let { it.shl(4).or(it) }.toByte()
                    }
                }
            }
            TextureFormat.RGB24 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        0 -> data[it / 4 * 3 + 2]
                        1 -> data[it / 4 * 3 + 1]
                        2 -> data[it / 4 * 3]
                        else -> -1
                    }
                }
            }
            TextureFormat.RGBA32 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        0 -> data[it + 2]
                        2 -> data[it - 2]
                        else -> data[it]
                    }
                }
            }
            TextureFormat.ARGB32 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        0 -> data[it + 3]
                        1 -> data[it + 1]
                        2 -> data[it - 1]
                        else -> data[it - 3]
                    }
                }
            }
            TextureFormat.RGB565 -> {
                swapForXBOX360(data)
                out = ByteArray(dataSize)
                for (i in areaIndices) {
                    val iu = i.toUInt()
                    val p = data[iu*2u].shl(8).or(data[iu*2u+1u])
                    out[i * 4] = p.shl(3).or(p.shr(2).and(7)).toByte()
                    out[i * 4 + 1] = p.shr(3).and(0xFC).or(p.shr(9).and(3)).toByte()
                    out[i * 4 + 2] = p.shr(8).and(0xF8).or(p.shr(13)).toByte()
                    out[i * 4 + 3] = -1
                }
            }
            TextureFormat.RGBA4444 -> {
                out = ByteArray(dataSize) { idx ->
                    when (idx % 4) {
                        0 -> data[(idx / 4 * 2 + 1).toUInt()].and(0xF0).shr(4)
                                .let { it.shl(4).or(it) }.toByte()
                        1 -> data[(idx / 4 * 2).toUInt()].and(0x0F)
                                .let { it.shl(4).or(it) }.toByte()
                        2 -> data[(idx / 4 * 2).toUInt()].and(0xF0).shr(4)
                                .let { it.shl(4).or(it) }.toByte()
                        else -> data[(idx / 4 * 2 + 1).toUInt()].and(0x0F)
                                .let { it.shl(4).or(it) }.toByte()
                    }
                }
            }
            TextureFormat.BGR24 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        0 -> data[it / 4 * 3]
                        1 -> data[it / 4 * 3 + 1]
                        2 -> data[it / 4 * 3 + 2]
                        else -> -1
                    }
                }
            }
            TextureFormat.BGRA32 -> {
                out = data.copyOf()
            }
            TextureFormat.RHalf -> {
                out = ByteArray(mWidth * mHeight * 4) {
                    val i = it.toUInt()
                    when (it % 4) {
                        2 -> (parseHalf(data, i / 4u * 2u) * 255f).roundToInt().toByte()
                        3 -> -1
                        else -> 0
                    }
                }
            }
            TextureFormat.RGHalf -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> 0
                        1 -> (parseHalf(data, i / 4u * 4u + 2u) * 255).roundToInt().toByte()
                        2 -> (parseHalf(data, i / 4u * 4u) * 255).roundToInt().toByte()
                        else -> -1
                    }
                }
            }
            TextureFormat.RGBAHalf -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> (parseHalf(data, i / 4u * 8u + 4u) * 255f).roundToInt().toByte()
                        1 -> (parseHalf(data, i / 4u * 8u + 2u) * 255f).roundToInt().toByte()
                        2 -> (parseHalf(data, i / 4u * 8u) * 255f).roundToInt().toByte()
                        else -> (parseHalf(data, i / 4u * 8u + 6u) * 255f).roundToInt().toByte()
                    }
                }
            }
            TextureFormat.RFloat -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        2 -> (parseFloat(data, i / 4u * 4u) * 255f).roundToInt().toByte()
                        3 -> -1
                        else -> 0
                    }
                }
            }
            TextureFormat.RGFloat -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> 0
                        1 -> (parseFloat(data, i / 4u * 8u + 4u) * 255f).roundToInt().toByte()
                        2 -> (parseFloat(data, i / 4u * 8u) * 255f).roundToInt().toByte()
                        else -> -1
                    }
                }
            }
            TextureFormat.RGBFloat -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> (parseFloat(data, i / 4u * 12u + 8u) * 255f).roundToInt().toByte()
                        1 -> (parseFloat(data, i / 4u * 12u + 4u) * 255f).roundToInt().toByte()
                        2 -> (parseFloat(data, i / 4u * 12u) * 255f).roundToInt().toByte()
                        else -> -1
                    }
                }
            }
            TextureFormat.RGBAFloat -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> (parseFloat(data, i / 4u * 16u + 8u) * 255f).roundToInt().toByte()
                        1 -> (parseFloat(data, i / 4u * 16u + 4u) * 255f).roundToInt().toByte()
                        2 -> (parseFloat(data, i / 4u * 16u) * 255f).roundToInt().toByte()
                        else -> (parseFloat(data, i / 4u * 16u + 12u) * 255f).roundToInt().toByte()
                    }
                }
            }
            TextureFormat.YUY2 -> {
                out = ByteArray(dataSize)
                var p = 0u; var o = 0
                for (y in 0 until mHeight) {
                    for (x in 0 until mWidth / 2) {
                        val y0 = data[p++]
                        val u0 = data[p++]
                        val y1 = data[p++]
                        val v0 = data[p++]
                        var c = y0 - 16
                        val d = u0 - 128
                        val e = v0 - 128
                        out[o++] = (298 * c + 516 * d + 128).shr(8).clampByte()
                        out[o++] = (298 * c - 100 * d - 208 * e + 128).shr(8).clampByte()
                        out[o++] = (298 * c + 409 * e + 128).shr(8).clampByte()
                        out[o++] = -1
                        c = y1 - 16
                        out[o++] = (298 * c + 516 * d + 128).shr(8).clampByte()
                        out[o++] = (298 * c - 100 * d - 208 * e + 128).shr(8).clampByte()
                        out[o++] = (298 * c + 409 * e + 128).shr(8).clampByte()
                        out[o++] = -1
                    }
                }
            }
            TextureFormat.RGB9e5Float -> {
                out = ByteArray(dataSize)
                for (i in dataSizeIndices) {
                    val n = parseInt(data, i.toUInt())
                    val scale = n.shr(27).and(0x1F)
                    val scalef = (2.0).pow(scale - 24)
                    val b = n.shr(18).and(0x1FF)
                    val g = n.shr(9).and(0x1FF)
                    val r = n.and(0x1FF)
                    out[i] = (b * scalef * 255).roundToInt().toByte()
                    out[i + 1] = (g * scalef * 255).roundToInt().toByte()
                    out[i + 2] = (r * scalef * 255).roundToInt().toByte()
                    out[i + 3] = -1
                }
            }
            TextureFormat.R8 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        2 -> data[it / 4]
                        3 -> -1
                        else -> 0
                    }
                }
            }
            TextureFormat.R16 -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        2 -> parseDownScaledInt8(data, i * 2u)
                        3 -> -1
                        else -> 0
                    }
                }
            }
            TextureFormat.RG16 -> {
                out = ByteArray(dataSize) {
                    when (it % 4) {
                        0 -> 0
                        1 -> data[it / 4 * 2 + 1]
                        2 -> data[it / 4 * 2]
                        else -> -1
                    }
                }
            }
            TextureFormat.RG32 -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> 0
                        1 -> parseDownScaledInt8(data, i / 4u * 4u + 2u)
                        2 -> parseDownScaledInt8(data, i / 4u * 4u)
                        else -> -1
                    }
                }
            }
            TextureFormat.RGB48 -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> parseDownScaledInt8(data, i / 4u * 6u + 4u)
                        1 -> parseDownScaledInt8(data, i / 4u * 6u + 2u)
                        2 -> parseDownScaledInt8(data, i / 4u * 6u)
                        else -> -1
                    }
                }
            }
            TextureFormat.RGBA64 -> {
                out = ByteArray(dataSize) {
                    val i = it.toUInt()
                    when (it % 4) {
                        0 -> parseDownScaledInt8(data, i / 4u * 8u + 4u)
                        1 -> parseDownScaledInt8(data, i / 4u * 8u + 2u)
                        2 -> parseDownScaledInt8(data, i / 4u * 8u)
                        else -> parseDownScaledInt8(data, i / 4u * 8u + 6u)
                    }
                }
            }
            TextureFormat.DXT1 -> {
                swapForXBOX360(data)
                out = ByteArray(dataSize)
                TextureDecoder.decodeDXT1(data, mWidth, mHeight, out)
            }
            TextureFormat.DXT1Crunched -> {
                out = ByteArray(dataSize)
                unpackCrunch(data)?.let {
                    TextureDecoder.decodeDXT1(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.DXT5 -> {
                swapForXBOX360(data)
                out = ByteArray(dataSize)
                TextureDecoder.decodeDXT5(data, mWidth, mHeight, out)
            }
            TextureFormat.DXT5Crunched -> {
                out = ByteArray(dataSize)
                unpackCrunch(data)?.let {
                    TextureDecoder.decodeDXT5(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.BC4 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeBC4(data, mWidth, mHeight, out)
            }
            TextureFormat.BC5 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeBC5(data, mWidth, mHeight, out)
            }
            TextureFormat.BC6H -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeBC6(data, mWidth, mHeight, out)
            }
            TextureFormat.BC7 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeBC7(data, mWidth, mHeight, out)
            }
            TextureFormat.PVRTC_RGB2, TextureFormat.PVRTC_RGBA2 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodePVRTC(data, mWidth, mHeight, out, true)
            }
            TextureFormat.PVRTC_RGB4, TextureFormat.PVRTC_RGBA4 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodePVRTC(data, mWidth, mHeight, out, false)
            }
            TextureFormat.ETC_RGB4, TextureFormat.ETC_RGB4_3DS -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeETC1(data, mWidth, mHeight, out)
            }
            TextureFormat.ATC_RGB4 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeATCRGB4(data, mWidth, mHeight, out)
            }
            TextureFormat.ATC_RGBA8 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeATCRGBA8(data, mWidth, mHeight, out)
            }
            TextureFormat.EAC_R -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeEACR(data, mWidth, mHeight, out)
            }
            TextureFormat.EAC_R_SIGNED -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeEACRSigned(data, mWidth, mHeight, out)
            }
            TextureFormat.EAC_RG -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeEACRG(data, mWidth, mHeight, out)
            }
            TextureFormat.EAC_RG_SIGNED -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeEACRGSigned(data, mWidth, mHeight, out)
            }
            TextureFormat.ETC2_RGB -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeETC2(data, mWidth, mHeight, out)
            }
            TextureFormat.ETC2_RGBA1 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeETC2A1(data, mWidth, mHeight, out)
            }
            TextureFormat.ETC2_RGBA8, TextureFormat.ETC_RGBA8_3DS -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeETC2A8(data, mWidth, mHeight, out)
            }
            TextureFormat.ETC_RGB4Crunched -> {
                out = ByteArray(dataSize)
                unpackCrunch(data)?.let {
                    TextureDecoder.decodeETC1(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.ETC2_RGBA8Crunched -> {
                out = ByteArray(dataSize)
                unpackCrunch(data)?.let {
                    TextureDecoder.decodeETC2A8(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.ASTC_RGB_4x4, TextureFormat.ASTC_RGBA_4x4, TextureFormat.ASTC_HDR_4x4 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeASTC(data, mWidth, mHeight, out, 4)
            }
            TextureFormat.ASTC_RGB_5x5, TextureFormat.ASTC_RGBA_5x5, TextureFormat.ASTC_HDR_5x5 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeASTC(data, mWidth, mHeight, out, 5)
            }
            TextureFormat.ASTC_RGB_6x6, TextureFormat.ASTC_RGBA_6x6, TextureFormat.ASTC_HDR_6x6 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeASTC(data, mWidth, mHeight, out, 6)
            }
            TextureFormat.ASTC_RGB_8x8, TextureFormat.ASTC_RGBA_8x8, TextureFormat.ASTC_HDR_8x8 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeASTC(data, mWidth, mHeight, out, 8)
            }
            TextureFormat.ASTC_RGB_10x10, TextureFormat.ASTC_RGBA_10x10, TextureFormat.ASTC_HDR_10x10 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeASTC(data, mWidth, mHeight, out, 10)
            }
            TextureFormat.ASTC_RGB_12x12, TextureFormat.ASTC_RGBA_12x12, TextureFormat.ASTC_HDR_12x12 -> {
                out = ByteArray(dataSize)
                TextureDecoder.decodeASTC(data, mWidth, mHeight, out, 12)
            }
            else -> {
                //Unknown, DXT3
                out = null
            }
        }
        return out
    }

    private fun unpackCrunch(data: ByteArray): ByteArray? {
        return if (
            unityVersion >= intArrayOf(2017, 3) ||
            mTextureFormat == TextureFormat.ETC_RGB4Crunched ||
            mTextureFormat == TextureFormat.ETC2_RGBA8Crunched
        ) {
            TextureDecoder.unpackUnityCrunch(data)
        } else {
            TextureDecoder.unpackCrunch(data)
        }
    }
}

internal class StreamingInfoImpl(reader: ObjectReader): StreamingInfo {
    override val offset = if (reader.unityVersion[0] >= 2020) reader.readInt64() else reader.readUInt32().toLong()
    override val size = reader.readUInt32()
    override val path = reader.readAlignedString()
}

internal class GLTextureSettingsImpl(reader: ObjectReader): GLTextureSettings {
    override val mFilterMode = reader.readInt32()
    override val mAniso = reader.readInt32()
    override val mMipBias = reader.readFloat()
    override val mWrapMode: Int

    init {
        if (reader.unityVersion[0] >= 2017) {
            mWrapMode = reader.readInt32()
            reader.skip(8)     //m_WrapV, m_WrapW: Int
        } else mWrapMode = reader.readInt32()
    }
}
