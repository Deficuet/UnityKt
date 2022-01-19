package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.extension.TextureDecoder
import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.*
import java.awt.image.*
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.math.roundToInt

class StreamingInfo internal constructor(reader: ObjectReader) {
    val offset = if (reader.unityVersion[0] >= 2020) reader.readLong() else reader.readUInt().toLong()
    val size = reader.readUInt()
    val path = reader.readAlignedString()
}

class GLTextureSettings internal constructor(reader: ObjectReader) {
    val mFilterMode = reader.readInt()
    val mAniso = reader.readInt()
    val mMipBias = reader.readFloat()
    val mWrapMode: Int

    init {
        if (reader.unityVersion[0] >= 2017) {
            mWrapMode = reader.readInt()
            reader += 8     //m_WrapV, m_WrapW: Int
        } else mWrapMode = reader.readInt()
    }
}

class Texture2DImpl internal constructor(reader: ObjectReader): TextureImpl(reader) {
    val mWidth = reader.readInt()
    val mHeight = reader.readInt()
    val mTextureFormat: TextureFormat
    val mMipMap: Boolean
    val mMipCount: Int
    val mTextureSettings: GLTextureSettings
    val imageData: ResourceReader
    val mStreamData: StreamingInfo?

    init {
        val version = reader.unityVersion
        reader += 4     //m_CompleteImageSize: Int
        if (version[0] >= 2020) reader += 4     //m_MipsStripped: Int
        mTextureFormat = TextureFormat.of(reader.readInt())
        if (version < intArrayOf(5, 2)) {
            mMipMap = reader.readBool()
            mMipCount = 0
        } else {
            mMipCount = reader.readInt()
            mMipMap = false
        }
        if (version >= intArrayOf(2, 6)) reader += 1        //m_IsReadable: Boolean
        if (version[0] >= 2020) reader += 1     //m_IsPreProcessed: Boolean
        if (version >= intArrayOf(2019, 3)) reader += 1     //m_IgnoreMasterTextureLimit: Boolean
        if (version[0] >= 3 && version <= intArrayOf(5, 4)) reader += 1      //m_ReadAllowed: Boolean
        if (version >= intArrayOf(2018, 2)) reader += 1     //m_StreamingMipmaps: Boolean
        reader.alignStream()
        if (version >= intArrayOf(2018, 2)) reader += 4     //m_StreamingMipmapsPriority: Int
        reader += 8     //m_ImageCount, m_TextureDimension: Int
        mTextureSettings = GLTextureSettings(reader)
        if (version[0] >= 3) reader += 4    //m_LightmapFormat: Int
        if (version >= intArrayOf(3, 5)) reader += 4    //m_ColorSpace: Int
        if (version >= intArrayOf(2020, 2)) {
            reader.readNextByteArray()      //m_PlatformBlob
            reader.alignStream()
        }
        val imageDataSize = reader.readInt()
        mStreamData = if (imageDataSize == 0 && version >= intArrayOf(5, 3)) {
            StreamingInfo(reader)
        } else null
        imageData = if (mStreamData?.path?.isNotEmpty() == true) {
            ResourceReader(mStreamData.path, assetFile, mStreamData.offset, mStreamData.size.toLong())
        } else {
            ResourceReader(reader, reader.absolutePosition, imageDataSize.toLong())
        }
    }

    val decompressedImageData by lazy { imageData.bytes.decompressTexture() }
    val image by lazy {
        BufferedImage(mWidth, mHeight, BufferedImage.TYPE_4BYTE_ABGR).apply {
            data = Raster.createRaster(
                ComponentSampleModel(
                    DataBuffer.TYPE_BYTE, mWidth, mHeight, 4,
                    mWidth * 4, intArrayOf(2, 1, 0, 3)
                ),
                DataBufferByte(decompressedImageData, decompressedImageData.size),
                null
            )
        }
    }

    private val areaIndices by lazy { 0 until mWidth * mHeight }
    private val dataSizeIndices by lazy { 0 until mWidth * mHeight * 4 step 4 }

    private fun ByteArray.swapForXbox() {
        if (platform == BuildTarget.XBOX360) reverse()
    }

    private fun ByteArray.decompressTexture(): ByteArray {
        val out = ByteArray(mWidth * mHeight * 4)

        when (mTextureFormat) {
            TextureFormat.Alpha8 -> {
                out.fill(-1)
                for (i in areaIndices) {
                    out[i * 4 + 3] = this[i]
                }
            }
            TextureFormat.ARGB4444 -> {
                swapForXbox()
                for (i in areaIndices) {
                    val p = ByteBuffer.wrap(this[i * 2, 2]).short.toIntBits()
                    val a = p.and(0x000f).let { it.shl(4).or(it) }
                    val b = p.and(0x00f0).shr(4).let { it.shl(4).or(it) }
                    val c = p.and(0x0f00).shr(8).let { it.shl(4).or(it) }
                    val d = p.and(0xf000).shr(12).let { it.shl(4).or(it) }
                    out[i * 4] = a.toByte()
                    out[i * 4 + 1] = b.toByte()
                    out[i * 4 + 2] = c.toByte()
                    out[i * 4 + 3] = d.toByte()
                }
            }
            TextureFormat.RGBA4444 -> {
                for (i in areaIndices) {
                    val p = ByteBuffer.wrap(this[i * 2, 2]).short.toIntBits()
                    val a = p.and(0x00f0).shr(4).let { it.shl(4).or(it) }
                    val b = p.and(0x0f00).shr(8).let { it.shl(4).or(it) }
                    val c = p.and(0xf000).shr(12).let { it.shl(4).or(it) }
                    val d = p.and(0x000f).let { it.shl(4).or(it) }
                    out[i * 4] = a.toByte()
                    out[i * 4 + 1] = b.toByte()
                    out[i * 4 + 2] = c.toByte()
                    out[i * 4 + 3] = d.toByte()
                }
            }
            TextureFormat.RGBA32 -> {
                for (i in dataSizeIndices) {
                    out[i] = this[i + 2]
                    out[i + 1] = this[i + 1]
                    out[i + 2] = this[i]
                    out[i + 3] = this[i + 3]
                }
            }
            TextureFormat.BGRA32 -> {
                return this
            }
            TextureFormat.ARGB32 -> {
                for (i in dataSizeIndices) {
                    out[i] = this[i + 3]
                    out[i + 1] = this[i + 2]
                    out[i + 2] = this[i + 1]
                    out[i + 3] = this[i + 0]
                }
            }
            TextureFormat.RGB24 -> {
                for (i in areaIndices) {
                    out[i * 4] = this[i * 3 + 2]
                    out[i * 4 + 1] = this[i * 3 + 1]
                    out[i * 4 + 2] = this[i * 3]
                    out[i * 4 + 3] = -1
                }
            }
            TextureFormat.RGB565 -> {
                swapForXbox()
                for (i in areaIndices) {
                    val p = ByteBuffer.wrap(this[i * 2, 2]).short.toIntBits()
                    out[i * 4] = p.shl(3).or(p.shr(2).and(7)).toByte()
                    out[i * 4 + 1] = p.shr(3).and(0xFC).or(p.shr(9).and(3)).toByte()
                    out[i * 4 + 2] = p.shr(8).and(0xF8).or(p.shr(13)).toByte()
                    out[i * 4 + 3] = -1
                }
            }
            TextureFormat.R8 -> {
                for (i in areaIndices) {
                    out[i * 4] = 0
                    out[i * 4 + 1] = 0
                    out[i * 4 + 2] = this[i]
                    out[i * 4 + 3] = -1
                }
            }
            TextureFormat.R16 -> {
                for (i in areaIndices) {
                    out[i * 4] = 0
                    out[i * 4 + 1] = 0
                    out[i * 4 + 2] = this[i * 2 + 1]
                    out[i * 4 + 3] = -1
                }
            }
            TextureFormat.RG16 -> {
                for (i in 0 until mWidth * mHeight step 2) {
                    out[i * 2] = 0
                    out[i * 2 + 1] = this[i + 1]
                    out[i * 2 + 2] = this[i]
                    out[i * 2 + 3] = -1
                }
            }
            TextureFormat.RHalf -> {
                for (i in dataSizeIndices) {
                    out[i] = 0
                    out[i + 1] = 0
                    out[i + 2] = (this[i / 2, 2].toHalf() * 255f).roundToInt().toByte()
                    out[i + 3] = -1
                }
            }
            TextureFormat.RGHalf -> {
                for (i in dataSizeIndices) {
                    out[i] = 0
                    out[i + 1] = (this[i + 2, 2].toHalf() * 255f).roundToInt().toByte()
                    out[i + 2] = (this[i, 2].toHalf() * 255f).roundToInt().toByte()
                    out[i + 3] = -1
                }
            }
            TextureFormat.RGBAHalf -> {
                for (i in dataSizeIndices) {
                    out[i] = (this[i * 2 + 4, 2].toHalf() * 255f).roundToInt().toByte()
                    out[i + 1] = (this[i * 2 + 2, 2].toHalf() * 255f).roundToInt().toByte()
                    out[i + 2] = (this[i * 2, 2].toHalf() * 255f).roundToInt().toByte()
                    out[i + 3] = (this[i * 2 + 6, 2].toHalf() * 255f).roundToInt().toByte()
                }
            }
            TextureFormat.RFloat -> {
                for (i in dataSizeIndices) {
                    out[i] = 0
                    out[i + 1] = 0
                    out[i + 2] = (ByteBuffer.wrap(this[i, 4]).float * 255f).roundToInt().toByte()
                    out[i + 3] = -1
                }
            }
            TextureFormat.RGFloat -> {
                for (i in dataSizeIndices) {
                    out[i] = 0
                    out[i + 2] = (ByteBuffer.wrap(this[i * 2 + 4, 4]).float * 255f).roundToInt().toByte()
                    out[i + 1] = (ByteBuffer.wrap(this[i * 2, 4]).float * 255f).roundToInt().toByte()
                    out[i + 3] = -1
                }
            }
            TextureFormat.RGBAFloat -> {
                for (i in dataSizeIndices) {
                    out[i] = (ByteBuffer.wrap(this[i * 4 + 8, 4]).float * 255f).roundToInt().toByte()
                    out[i + 1] = (ByteBuffer.wrap(this[i * 4 + 4, 4]).float * 255f).roundToInt().toByte()
                    out[i + 2] = (ByteBuffer.wrap(this[i * 4, 4]).float * 255f).roundToInt().toByte()
                    out[i + 3] = (ByteBuffer.wrap(this[i * 4 + 12, 4]).float * 255f).roundToInt().toByte()
                }
            }
            TextureFormat.YUY2 -> {
                var p = 0u; var o = 0
                for (y in 0 until mHeight) {
                    for (x in 0 until mWidth / 2) {
                        val y0 = this[p++]
                        val u0 = this[p++]
                        val y1 = this[p++]
                        val v0 = this[p++]
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
                for (i in dataSizeIndices) {
                    val n = ByteBuffer.wrap(this[i, 4]).int
                    val scale = n.shr(27).and(0x1F)
                    val scalef = 2.0.pow(scale - 24)
                    val b = n.shr(18).and(0x1FF)
                    val g = n.shr(9).and(0x1FF)
                    val r = n.and(0x1FF)
                    out[i] = (b * scalef * 255).roundToInt().toByte()
                    out[i + 1] = (g * scalef * 255).roundToInt().toByte()
                    out[i + 2] = (r * scalef * 255).roundToInt().toByte()
                    out[i + 3] = -1
                }
            }
            TextureFormat.DXT1 -> {
                swapForXbox()
                TextureDecoder.decodeDXT1(this, mWidth, mHeight, out)
            }
            TextureFormat.DXT1Crunched -> {
                unpackCrunch()?.let {
                    TextureDecoder.decodeDXT1(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.DXT5 -> {
                swapForXbox()
                TextureDecoder.decodeDXT5(this, mWidth, mHeight, out)
            }
            TextureFormat.DXT5Crunched -> {
                unpackCrunch()?.let {
                    TextureDecoder.decodeDXT5(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.BC4 -> {
                TextureDecoder.decodeBC4(this, mWidth, mHeight, out)
            }
            TextureFormat.BC5 -> {
                TextureDecoder.decodeBC5(this, mWidth, mHeight, out)
            }
            TextureFormat.BC6H -> {
                TextureDecoder.decodeBC6(this, mWidth, mHeight, out)
            }
            TextureFormat.BC7 -> {
                TextureDecoder.decodeBC7(this, mWidth, mHeight, out)
            }
            TextureFormat.PVRTC_RGB2, TextureFormat.PVRTC_RGBA2 -> {
                TextureDecoder.decodePVRTC(this, mWidth, mHeight, out, true)
            }
            TextureFormat.PVRTC_RGB4, TextureFormat.PVRTC_RGBA4 -> {
                TextureDecoder.decodePVRTC(this, mWidth, mHeight, out, false)
            }
            TextureFormat.ETC_RGB4, TextureFormat.ETC_RGB4_3DS -> {
                TextureDecoder.decodeETC1(this, mWidth, mHeight, out)
            }
            TextureFormat.ETC2_RGB -> {
                TextureDecoder.decodeETC2(this, mWidth, mHeight, out)
            }
            TextureFormat.ETC2_RGBA1 -> {
                TextureDecoder.decodeETC2A1(this, mWidth, mHeight, out)
            }
            TextureFormat.ETC2_RGBA8, TextureFormat.ETC_RGBA8_3DS -> {
                TextureDecoder.decodeETC2A8(this, mWidth, mHeight, out)
            }
            TextureFormat.ETC_RGB4Crunched -> {
                unpackCrunch()?.let {
                    TextureDecoder.decodeETC1(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.ETC2_RGBA8Crunched -> {
                unpackCrunch()?.let {
                    TextureDecoder.decodeETC2A8(it, mWidth, mHeight, out)
                }
            }
            TextureFormat.ATC_RGB4 -> {
                TextureDecoder.decodeATCRGB4(this, mWidth, mHeight, out)
            }
            TextureFormat.ATC_RGBA8 -> {
                TextureDecoder.decodeATCRGBA8(this, mWidth, mHeight, out)
            }
            TextureFormat.ASTC_RGB_4x4, TextureFormat.ASTC_RGBA_4x4, TextureFormat.ASTC_HDR_4x4 -> {
                TextureDecoder.decodeASTC(this, mWidth, mHeight, out, 4)
            }
            TextureFormat.ASTC_RGB_5x5, TextureFormat.ASTC_RGBA_5x5, TextureFormat.ASTC_HDR_5x5 -> {
                TextureDecoder.decodeASTC(this, mWidth, mHeight, out, 5)
            }
            TextureFormat.ASTC_RGB_6x6, TextureFormat.ASTC_RGBA_6x6, TextureFormat.ASTC_HDR_6x6 -> {
                TextureDecoder.decodeASTC(this, mWidth, mHeight, out, 6)
            }
            TextureFormat.ASTC_RGB_8x8, TextureFormat.ASTC_RGBA_8x8, TextureFormat.ASTC_HDR_8x8 -> {
                TextureDecoder.decodeASTC(this, mWidth, mHeight, out, 8)
            }
            TextureFormat.ASTC_RGB_10x10, TextureFormat.ASTC_RGBA_10x10, TextureFormat.ASTC_HDR_10x10 -> {
                TextureDecoder.decodeASTC(this, mWidth, mHeight, out, 10)
            }
            TextureFormat.ASTC_RGB_12x12, TextureFormat.ASTC_RGBA_12x12, TextureFormat.ASTC_HDR_12x12 -> {
                TextureDecoder.decodeASTC(this, mWidth, mHeight, out, 12)
            }
            TextureFormat.EAC_R -> {
                TextureDecoder.decodeEACR(this, mWidth, mHeight, out)
            }
            TextureFormat.EAC_R_SIGNED -> {
                TextureDecoder.decodeEACRSigned(this, mWidth, mHeight, out)
            }
            TextureFormat.EAC_RG -> {
                TextureDecoder.decodeEACRG(this, mWidth, mHeight, out)
            }
            TextureFormat.EAC_RG_SIGNED -> {
                TextureDecoder.decodeEACRGSigned(this, mWidth, mHeight, out)
            }
            else -> {  }
        }
        return out
    }

    private fun unpackCrunch(): ByteArray? {
        return if (
            unityVersion >= intArrayOf(2017, 3) ||
            mTextureFormat == TextureFormat.ETC_RGB4Crunched ||
            mTextureFormat == TextureFormat.ETC2_RGBA8Crunched
        ) {
            TextureDecoder.unpackUnityCrunch(imageData.bytes)
        } else {
            TextureDecoder.unpackCrunch(imageData.bytes)
        }
    }
}

@Suppress("EnumEntryName")
enum class TextureFormat(val id: Int) {
    Unknown(0),
    Alpha8(1),
    ARGB4444(2),
    RGB24(3),
    RGBA32(4),
    ARGB32(5),
    RGB565(7),
    R16(9),
    DXT1(10),
    DXT5(12),
    RGBA4444(13),
    BGRA32(14),
    RHalf(15),
    RGHalf(16),
    RGBAHalf(17),
    RFloat(18),
    RGFloat(19),
    RGBAFloat(20),
    YUY2(21),
    RGB9e5Float(22),
    BC4(26),
    BC5(27),
    BC6H(24),
    BC7(25),
    DXT1Crunched(28),
    DXT5Crunched(29),
    PVRTC_RGB2(30),
    PVRTC_RGBA2(31),
    PVRTC_RGB4(32),
    PVRTC_RGBA4(33),
    ETC_RGB4(34),
    ATC_RGB4(35),
    ATC_RGBA8(36),
    EAC_R(41),
    EAC_R_SIGNED(42),
    EAC_RG(43),
    EAC_RG_SIGNED(44),
    ETC2_RGB(45),
    ETC2_RGBA1(46),
    ETC2_RGBA8(47),
    ASTC_RGB_4x4(48),
    ASTC_RGB_5x5(49),
    ASTC_RGB_6x6(50),
    ASTC_RGB_8x8(51),
    ASTC_RGB_10x10(52),
    ASTC_RGB_12x12(53),
    ASTC_RGBA_4x4(54),
    ASTC_RGBA_5x5(55),
    ASTC_RGBA_6x6(56),
    ASTC_RGBA_8x8(57),
    ASTC_RGBA_10x10(58),
    ASTC_RGBA_12x12(59),
    ETC_RGB4_3DS(60),
    ETC_RGBA8_3DS(61),
    RG16(62),
    R8(63),
    ETC_RGB4Crunched(64),
    ETC2_RGBA8Crunched(65),
    ASTC_HDR_4x4(66),
    ASTC_HDR_5x5(67),
    ASTC_HDR_6x6(68),
    ASTC_HDR_8x8(69),
    ASTC_HDR_10x10(70),
    ASTC_HDR_12x12(71),
    RG32(72),
    RGB48(73),
    RGBA64(74);

    companion object {
        fun of(value: Int): TextureFormat {
            return values().firstOrNull { it.id == value } ?: Unknown
        }
    }
}