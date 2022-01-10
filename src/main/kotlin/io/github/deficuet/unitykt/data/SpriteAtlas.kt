package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class SpriteAtlas internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mPackedSprites = reader.readArrayOf { PPtr<Sprite>(reader) }
    val mRenderDataMap: Map<Pair<ByteArray, Long>, SpriteAtlasData>
    val mIsVariant: Boolean

    init {
        reader.readNextStringArray()    //m_PackedSpriteNamesToIndex
        mRenderDataMap = reader.readArrayOf {
            val first = reader.read(16)
            val second = reader.readLong()
            val value = SpriteAtlasData(reader)
            (first to second) to value
        }.toMap()
        reader.readAlignedString()      //m_Tag
        mIsVariant = reader.readBool()
        reader.alignStream()
    }
}

class SpriteAtlasData internal constructor(reader: ObjectReader) {
    val texture = PPtr<Texture2D>(reader)
    val alphaText = PPtr<Texture2D>(reader)
    val textureRect = reader.readRectangle()
    val textureRectOffset = reader.readVector2()
    val atlasRectOffset = if (reader.unityVersion >= intArrayOf(2017, 2)) {
        reader.readVector2()
    } else Vector2.Zero
    val uvTransform = reader.readVector4()
    val downScaleMultiplier = reader.readFloat()
    val settingsRaw = SpriteSettings(reader)
    val secondaryTextures: Array<SecondarySpriteTexture>

    init {
        if (reader.unityVersion >= intArrayOf(2020, 2)) {
            secondaryTextures = reader.readArrayOf { SecondarySpriteTexture(reader) }
            reader.alignStream()
        } else secondaryTextures = emptyArray()
    }
}