package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.ResourceReader
import io.github.deficuet.unitykt.util.compareTo

class Texture2D internal constructor(reader: ObjectReader): Texture(reader) {
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
            ResourceReader(mStreamData.path, asserFile, mStreamData.offset, mStreamData.size.toLong())
        } else {
            ResourceReader(reader, reader.absolutePosition, imageDataSize.toLong())
        }
    }

    val decompressedImageData by lazy { imageData.bytes.decompressTexture() }

    private fun ByteArray.decompressTexture(): ByteArray {
        val out = ByteArray(mWidth * mHeight * 4)
        when (mTextureFormat) {
            TextureFormat.RGBA32 -> {
                var pos = 0; var outPos = 0
                for (x in 0 until mHeight) {
                    for (y in 0 until mWidth) {
                        out[outPos] = this[pos]
                        out[outPos + 1] = this[pos + 1]
                        out[outPos + 2] = this[pos + 2]
                        out[outPos + 3] = this[pos + 3]
                        pos += 4; outPos += 4
                    }
                }
            }
            else -> {  }
        }
        return out
    }
}

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