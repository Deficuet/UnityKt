package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Material
import io.github.deficuet.unitykt.data.Texture
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class FontImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mFontData: ByteArray

    init {
        if (unityVersion >= intArrayOf(5, 5)) {
            reader += 4     //m_LineSpacing: Float
            PPtr<Material>(reader)      //m_DefaultMaterial
            reader += 4     //m_FontSize: Float
            PPtr<Texture>(reader)      //m_Texture
            //m_AsciiStartOffset: Int, m_Tracking: Float m_CharacterSpacing: Int,
            //m_CharacterPadding: Int, m_ConvertCase: Int
            reader += 20
            val characterRectSize = reader.readInt()
            for (i in 1..characterRectSize) {
                reader += 44
            }
            val kerningValuesSize = reader.readInt()
            for (j in 1..kerningValuesSize) {
                reader += 8
            }
            reader += 4     //m_PixelScale: Float
            val fontDataSize = reader.readInt()
            mFontData = if (fontDataSize > 0) {
                reader.read(fontDataSize)
            } else byteArrayOf()
        } else {
            reader += 4     //m_AsciiStartOffset: Int
            if (unityVersion[0] <= 3) {
                reader += 8     //m_FontCountX, m_FontCountY: Int
            }
            reader += 8     //m_Kerning, m_LineSpacing: Float
            if (unityVersion[0] <= 3) {
                reader.readArrayOf {
                    reader += 8     //first: Int, second: Float
                }
            } else {
                reader += 8     //m_CharacterSpacing, m_CharacterPadding: Int
            }
            reader += 4     //m_ConvertCase: Int
            PPtr<Material>(reader)
            val charRectSize = reader.readInt()
            for (i in 1..charRectSize) {
                reader += 40
                if (unityVersion[0] >= 4) {
                    reader += 1     //flipped: Boolean
                    reader.alignStream()
                }
            }
            PPtr<Texture>(reader)
            val kerningValueSize = reader.readInt()
            for (j in 1..kerningValueSize) {
                reader += 8
            }
            if (unityVersion[0] <= 3) {
                reader += 1     //m_GridFont
                reader.alignStream()
            } else {
                reader += 4     //m_PixelScale
            }
            val fontDataSize = reader.readInt()
            mFontData = if (fontDataSize > 0) reader.read(fontDataSize) else byteArrayOf()
        }
    }
}