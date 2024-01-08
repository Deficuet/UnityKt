package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.classes.Sprite
import io.github.deficuet.unitykt.classes.SpriteAtlas
import io.github.deficuet.unitykt.classes.SpriteAtlasData
import io.github.deficuet.unitykt.classes.Texture2D
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.pptr.safeGetObj
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf
import java.nio.ByteBuffer
import java.util.*

internal class SpriteAtlasImpl(
    assetFile: SerializedFile, info: ObjectInfo
): SpriteAtlas, SpriteAtlasFields(assetFile, info) {
    override val mPackedSprites: Array<PPtrImpl<Sprite>> get() {
        checkInitialize()
        return fmPackedSprites
    }
    override val mRenderDataMap: Map<Pair<UUID, Long>, SpriteAtlasData> get() {
        checkInitialize()
        return fmRenderDataMap
    }
    override val mIsVariant: Boolean get() {
        checkInitialize()
        return fmIsVariant
    }

    override fun read() {
        super.read()
        fmPackedSprites = reader.readArrayOf { PPtrImpl(this) }
        reader.readAlignedStringArray()    //m_PackedSpriteNamesToIndex
        val buffer = ByteBuffer.allocate(16)
        fmRenderDataMap = reader.readArrayOf {
            buffer.put(reader.read(16))
            val second = reader.readInt64()
            val value = SpriteAtlasDataImpl(reader)
            val ret = (UUID(buffer.long, buffer.long) to second) to value
            buffer.clear()
            ret
        }.toMap()
        reader.readAlignedString()      //m_Tag
        fmIsVariant = reader.readBool()
        reader.alignStream()
        for (packedSprite in fmPackedSprites) {
            val sprite = packedSprite.safeGetObj()
            if (sprite != null) {
                val sa = sprite.mSpriteAtlas
                if (sa != null) {
                    if (sa.isNull) {
                        sa.cast<PPtrImpl<SpriteAtlas>>().setObj(this)
                    } else {
                        val old = sa.safeGetObj()
                        if (old != null && old.mIsVariant) {
                            sa.cast<PPtrImpl<SpriteAtlas>>().setObj(this)
                        }
                    }
                }
            }
        }
    }
}

internal class SpriteAtlasDataImpl(reader: ObjectReader): SpriteAtlasData {
    override val texture = PPtrImpl<Texture2D>(reader)
    override val alphaText = PPtrImpl<Texture2D>(reader)
    override val textureRect = reader.readRectangle()
    override val textureRectOffset = reader.readVector2()
    override val atlasRectOffset = if (reader.unityVersion >= intArrayOf(2017, 2)) {
        reader.readVector2()
    } else Vector2.Zero
    override val uvTransform = reader.readVector4()
    override val downScaleMultiplier = reader.readFloat()
    override val settingsRaw = SpriteSettingsImpl(reader)
    override val secondaryTextures: Array<SecondarySpriteTextureImpl>

    init {
        if (reader.unityVersion >= intArrayOf(2020, 2)) {
            secondaryTextures = reader.readArrayOf { SecondarySpriteTextureImpl(this) }
            reader.alignStream()
        } else secondaryTextures = emptyArray()
    }
}
