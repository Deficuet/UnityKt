package io.github.deficuet.unitykt.extension

import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayReader
import java.nio.ByteBuffer

class Crn private constructor() {
    @Suppress("EnumEntryName")
    private enum class Format(val id: Int, val bitsPerTexel: Int, val bytesPerBlock: Int) {
        cCRNFmtInvalid(-1, 0, 0),
        cCRNFmtDXT1(0, 4, 8),
        cCRNFmtFirstValid(0, 0, 0),
        cCRNFmtDXT3(1, 8, 16),
        cCRNFmtDXT5(2, 8, 16),
        cCRNFmtDXT5_CCxY(3, 8, 16),
        cCRNFmtDXT5_xGxR(4, 8, 16),
        cCRNFmtDXT5_xGBR(5, 8, 16),
        cCRNFmtDXT5_AGBR(6, 8, 16),
        cCRNFmtDXN_XY(7, 8, 16),
        cCRNFmtDXN_YX(8, 8, 16),
        cCRNFmtDXT5A(9, 4, 8),
        cCRNFmtETC1(10, 4, 8),
        cCRNFmtTotal(11, 0, 0),
        cCRNFmtForceDWORD(0x7FFFFFFF, 0, 0);

        companion object {
            fun of(value: Int): Format {
                return values().firstOrNull { it.id == value } ?: cCRNFmtInvalid
            }
        }
    }

    private class Header(reader: EndianBinaryReader) {
        val mSig = reader.readUShort().toUInt()
        val mHeaderSize = reader.readUShort()
        val mHeaderCrc16 = reader.readUShort()
        val mDataSize = reader.readUInt()
        val mDataCrc16 = reader.readUShort()
        val mWidth = reader.readUShort()
        val mHeight = reader.readUShort()
        val mLevels = reader.readByte()
        val mFaces = reader.readByte()
        val mFormat = Format.of(reader.readByte().toInt())
        val mFlags = reader.readUShort()
        val mReserved = reader.readUInt()
        val mUserdata0 = reader.readUInt()
        val mUserdata1 = reader.readUInt()
        val mColorEndpoints = Palette(reader)
        val mColorSelectors = Palette(reader)
        val mAlphaEndpoints = Palette(reader)
        val mAlphaSelectors = Palette(reader)
        val mTablesSize = reader.readUShort()
        val mTablesOfs = reader.readBytesFillToUInt(3)
        val mLevelOfs = reader.readUInt()// = Array(mLevels.toInt()) { 0u }.apply { this[1] }

        companion object {
            const val size = 74u
            const val cCRNSigValue = 18552u
        }
    }

    private class Palette(reader: EndianBinaryReader) {
        val mOfs = reader.readBytesFillToUInt(3)
        val mSize = reader.readBytesFillToUInt(3)
        val mNum = reader.readUShort()
    }

    private data class TextureInfo(
        val mWidth: UInt,
        val mHeight: UInt,
        val mLevels: UInt,
        val mFaces: UInt,
        val mBytesPerBlock: UInt,
        val mUserdata0: UInt,
        val mUserdata1: UInt,
        val mFormat: Format
    )

    companion object {
        fun crunchUnpackLevel(data: ByteArray, dataSize: Int, level: Int): ByteArray? {
            val info = getTextureInfo(data, dataSize) ?: return null
            return null
        }

        private fun getTextureInfo(data: ByteArray, dataSize: Int): TextureInfo? {
            if (dataSize.toUInt() < Header.size) return null
            val header = getHeader(data, dataSize) ?: return null
            return with(header) {
                TextureInfo(
                    mWidth = mWidth.toUInt(),
                    mHeight = mHeight.toUInt(),
                    mLevels = mLevels.toUInt(),
                    mFaces = mFaces.toUInt(),
                    mFormat = mFormat,
                    mBytesPerBlock = if (mFormat == Format.cCRNFmtDXT1 || mFormat == Format.cCRNFmtDXT5A) 8u else 16u,
                    mUserdata0 = mUserdata0,
                    mUserdata1 = mUserdata1
                )
            }
        }

        private fun getHeader(data: ByteArray, dataSize: Int): Header? {
            if (dataSize.toUInt() < Header.size) return null
            val header = EndianByteArrayReader(data).use { Header(it) }
            if (header.mSig != Header.cCRNSigValue) return null
            if (header.mHeaderSize < Header.size || dataSize.toUInt() < header.mDataSize) return null
            return header
        }

        private fun EndianBinaryReader.readBytesFillToUInt(bits: Int): UInt {
            val data = read(bits)
            val bytes = ByteArray(4)
            for (i in 0..3) {
                bytes[i] = if (i < 4 - bits) 0 else data[i - 4 + bits]
            }
            return ByteBuffer.wrap(bytes).int.toUInt()
        }
    }
}