package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.jimage.*
import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Rectangle
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.pptr.safeGetObj
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal class SpriteImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Sprite, SpriteFields(assetFile, info) {
    override val mRect: Rectangle get() {
        checkInitialize()
        return fmRect
    }
    override val mOffset: Vector2 get() {
        checkInitialize()
        return fmOffset
    }
    override val mBorder: Vector4 get() {
        checkInitialize()
        return fmBorder
    }
    override val mPixelsToUnits: Float get() {
        checkInitialize()
        return fmPixelsToUnits
    }
    override val mPivot: Vector2 get() {
        checkInitialize()
        return fmPivot
    }
    override val mExtrude: UInt get() {
        checkInitialize()
        return fmExtrude
    }
    override val mIsPolygon: Boolean get() {
        checkInitialize()
        return fmIsPolygon
    }
    override val mRenderDataKey: Pair<UUID, Long> get() {
        checkInitialize()
        return fmRenderDataKey
    }
    override val mAtlasTags: Array<String> get() {
        checkInitialize()
        return fmAtlasTags
    }
    override val mSpriteAtlas: PPtr<SpriteAtlas>? get() {
        checkInitialize()
        return fmSpriteAtlas
    }
    override val mRD: SpriteRenderData get() {
        checkInitialize()
        return fmRD
    }
    override val mPhysicsShape: Array<Array<Vector2>> get() {
        checkInitialize()
        return fmPhysicsShape
    }

    override fun read() {
        super.read()
        val version = reader.unityVersion
        fmRect = reader.readRectangle()
        fmOffset = reader.readVector2()
        fmBorder = if (version >= intArrayOf(4, 5)) reader.readVector4() else Vector4.Zero
        fmPixelsToUnits = reader.readFloat()
        fmPivot = if (version >= intArrayOf(5, 4, 1, 3)) {
            reader.readVector2()
        } else Vector2(0.5f, 0.5f)
        fmExtrude = reader.readUInt32()
        if (version >= intArrayOf(5, 3)) {
            fmIsPolygon = reader.readBool()
            reader.alignStream()
        } else {
            fmIsPolygon = false
        }
        if (version[0] >= 2017) {
            fmRenderDataKey = ByteBuffer.wrap(reader.read(16)).let {
                val ret = UUID(it.long, it.long)
                it.clear()
                ret
            } to reader.readInt64()
            fmAtlasTags = reader.readAlignedStringArray()
            fmSpriteAtlas = PPtrImpl(reader)
        } else {
            fmRenderDataKey = Pair(UUID(0L, 0L), 0L)
            fmAtlasTags = emptyArray()
            fmSpriteAtlas = null
        }
        fmRD = SpriteRenderDataImpl(reader)
        fmPhysicsShape = if (version[0] >= 2017) {
            reader.readNestedVector2Array()
        } else emptyArray()
    }

    override fun getTexture2D(): Texture2D? {
        val spriteAtlas = mSpriteAtlas?.safeGetObj()
        return if (spriteAtlas != null) {
            spriteAtlas.mRenderDataMap[mRenderDataKey]?.texture?.safeGetObj()
        } else {
            mRD.texture.safeGetObj()
        }
    }

    override fun getImage(): BufferedImage? {
        val spriteAtlas = mSpriteAtlas?.safeGetObj()
        if (spriteAtlas != null) {
            val spriteAtlasData = spriteAtlas.mRenderDataMap[mRenderDataKey]
            val tex = spriteAtlasData?.texture?.safeGetObj()
            if (tex != null) {
                println(tex.mPathID)
                return cutImage(
                    tex, spriteAtlasData.textureRect, //spriteAtlasData.textureRectOffset,
                    spriteAtlasData.downScaleMultiplier, spriteAtlasData.settingsRaw
                )
            }
        } else {
            val tex = mRD.texture.safeGetObj()
            if (tex != null) {
                return with(mRD) {
                    cutImage(
                        tex, textureRect, //textureRectOffset,
                        downScaleMultiplier, settingsRaw
                    )
                }
            }
        }
        return null
    }

    private fun cutImage(
        tex: Texture2D,
        rect: Rectangle,
//        offset: Vector2,
        downscaleMultiplier: Float,
        settingsRow: SpriteSettings
    ): BufferedImage? {
        var image = tex.getImage()
        if (image != null) {
            if (downscaleMultiplier > 0f && downscaleMultiplier != 1f) {
                val w = (tex.mWidth * downscaleMultiplier).roundToInt()
                val h = (tex.mHeight * downscaleMultiplier).roundToInt()
                image = image.resize(w, h)
            }
            val x = floor(rect.x).toInt()
            val y = floor(rect.y).toInt()
            val right = minOf(ceil(rect.x + rect.w).toInt(), image.width)
            val bottom = minOf(ceil(rect.y + rect.h).toInt(), image.height)
            var spriteImage = image.getSubimage(x, y, right - x, bottom - y)
            if (settingsRow.packed == 1u) {
                when (settingsRow.packingRotation) {
                    SpritePackingRotation.FlipHorizontal -> {
                        spriteImage = spriteImage.flipX().apply(true)
                    }
                    SpritePackingRotation.FlipVertical -> {
                        spriteImage = spriteImage.flipY().apply(true)
                    }
                    SpritePackingRotation.Rotate90 -> {
                        spriteImage = spriteImage.quadrantRotate(3).apply(true)
                    }
                    SpritePackingRotation.Rotate180 -> {
                        spriteImage = spriteImage.quadrantRotate(2).apply(true)
                    }
                    else -> {  }
                }
            }
//            if (settingsRow.packingMode == SpritePackingMode.Tight) {
//                TODO("Sprite image tight")
//            }
            return spriteImage
        }
        return null
    }
}

internal class SecondarySpriteTextureImpl(reader: ObjectReader): SecondarySpriteTexture {
    override val texture = PPtrImpl<Texture2D>(reader)
    override val name = reader.readNullString()
}

internal class SpriteSettingsImpl(reader: ObjectReader): SpriteSettings {
    override val settingsRaw = reader.readUInt32()
    override val packed = settingsRaw.and(1u)
    override val packingMode = SpritePackingMode.of(settingsRaw.shr(1).and(1u))
    override val packingRotation = SpritePackingRotation.of(settingsRaw.shr(2).and(0xFu))
    override val meshType = SpriteMeshType.of(settingsRaw.shr(6).and(1u))
}

internal class SpriteVertexImpl(reader: ObjectReader): SpriteVertex {
    override val pos = reader.readVector3()
    override val uv = if (reader.unityVersion <= intArrayOf(4, 3)) reader.readVector2() else Vector2.Zero
}

internal class SpriteRenderDataImpl(reader: ObjectReader): SpriteRenderData {
    override val texture = PPtrImpl<Texture2D>(reader)
    override val alphaTexture = if (reader.unityVersion >= intArrayOf(5, 2)) PPtrImpl<Texture2D>(reader) else null
    override val secondaryTexturesSize = if (reader.unityVersion[0] >= 2019) {
        reader.readArrayOf { SecondarySpriteTextureImpl(this) }
    } else emptyArray()
    override val mSubMeshes: Array<SubMashImpl>
    override val mIndexBuffer: ByteArray
    override val mVertexData: VertexData?
    override val vertices: Array<SpriteVertexImpl>
    override val indices: Array<UShort>
    override val mBindPose: Array<Matrix4x4>
    override val mSourceSkin: Array<BoneWeights4Impl>
    override val textureRect: Rectangle
    override val textureRectOffset: Vector2
    override val atlasRectOffset: Vector2
    override val settingsRaw: SpriteSettings
    override val uvTransform: Vector4
    override val downScaleMultiplier: Float

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(5, 6)) {
            mSubMeshes = reader.readArrayOf { SubMashImpl(this) }
            mIndexBuffer = reader.readInt8Array()
            reader.alignStream()
            mVertexData = VertexDataImpl(reader)
            vertices = emptyArray()
            indices = emptyArray()
        } else {
            vertices = reader.readArrayOf { SpriteVertexImpl(this) }
            indices = reader.readUInt16Array()
            reader.alignStream()
            mSubMeshes = emptyArray()
            mIndexBuffer = ByteArray(0)
            mVertexData = null
        }
        if (version[0] >= 2018) {
            mBindPose = reader.readMatrix4x4Array()
            mSourceSkin = if (version[0] == 2018 && version[1] < 2) {
                reader.readArrayOf { BoneWeights4Impl(this) }
            } else emptyArray()
        } else {
            mBindPose = emptyArray()
            mSourceSkin = emptyArray()
        }
        textureRect = reader.readRectangle()
        textureRectOffset = reader.readVector2()
        atlasRectOffset = if (version >= intArrayOf(5, 6)) {
            reader.readVector2()
        } else Vector2.Zero
        settingsRaw = SpriteSettingsImpl(reader)
        uvTransform = if (version >= intArrayOf(4, 5)) {
            reader.readVector4()
        } else Vector4.Zero
        downScaleMultiplier = if (version[0] >= 2017) {
            reader.readFloat()
        } else 0f
    }
}