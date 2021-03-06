package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.data.SpriteAtlas
import io.github.deficuet.unitykt.data.Texture2D
import io.github.deficuet.unitykt.math.Matrix4x4
import io.github.deficuet.unitykt.math.Rectangle
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class SpriteImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mRect = reader.readRectangle()
    val mOffset = reader.readVector2()
    val mBorder = if (reader.unityVersion >= intArrayOf(4, 5)) reader.readVector4() else Vector4.Zero
    val mPixelsToUnits = reader.readFloat()
    val mPivot = if (reader.unityVersion >= intArrayOf(5, 4, 1, 3)) {
        reader.readVector2()
    } else Vector2(0.5, 0.5)
    val mExtrude = reader.readUInt()
    val mIsPolygon: Boolean
    val mRenderDataKey: Map<ByteArray, Long>
    val mAtlasTags: Array<String>
    val mSpriteAtlas: PPtr<SpriteAtlas>?
    val mRD: SpriteRenderData
    val mPhysicsShape: Array<Array<Vector2>>

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(5, 3)) {
            mIsPolygon = reader.readBool()
            reader.alignStream()
        } else {
            mIsPolygon = false
        }
        if (version[0] >= 2017) {
            mRenderDataKey = mapOf(reader.read(16) to reader.readLong())
            mAtlasTags = reader.readNextStringArray()
            mSpriteAtlas = PPtr(reader)
        } else {
            mRenderDataKey = emptyMap()
            mAtlasTags = emptyArray()
            mSpriteAtlas = null
        }
        mRD = SpriteRenderData(reader)
        mPhysicsShape = if (version[0] >= 2017) {
            reader.readArrayOf { reader.readNextVector2Array() }
        } else emptyArray()
    }
}

class SecondarySpriteTexture internal constructor(reader: ObjectReader) {
    val texture = PPtr<Texture2D>(reader)
    val name = reader.readStringUntilNull()
}

@Suppress("EnumEntryName")
enum class SpritePackingRotation(val id: UInt) {
    kSPRNone(0u),
    kSPRFlipHorizontal(1u),
    kSPRFlipVertical(2u),
    kSPRRotate180(3u),
    kSPRRotate90(4u);

    companion object {
        fun of(value: UInt): SpritePackingRotation {
            return values().firstOrNull { it.id == value } ?: kSPRNone
        }
    }
}

@Suppress("EnumEntryName")
enum class SpritePackingMode(val id: UInt) {
    kSPMTight(0u),
    kSPMRectangle(1u);

    companion object {
        fun of(value: UInt): SpritePackingMode {
            return values().firstOrNull { it.id == value } ?: kSPMTight
        }
    }
}

@Suppress("EnumEntryName")
enum class SpriteMeshType(val id: UInt) {
    kSpriteMeshTypeFullRect(0u),
    kSpriteMeshTypeTight(1u);

    companion object {
        fun of(value: UInt): SpriteMeshType {
            return values().firstOrNull { it.id == value } ?: kSpriteMeshTypeFullRect
        }
    }
}

class SpriteSettings internal constructor(reader: ObjectReader) {
    val settingsRaw = reader.readUInt()
    val packed = settingsRaw.and(1u)
    val packingMode = SpritePackingMode.of(settingsRaw.shr(1).and(1u))
    val packingRotation = SpritePackingRotation.of(settingsRaw.shr(2).and(0xFu))
    val meshType = SpriteMeshType.of(settingsRaw.shr(6).and(1u))
}

class SpriteVertex internal constructor(reader: ObjectReader) {
    val pos = reader.readVector3()
    val uv = if (reader.unityVersion <= intArrayOf(4, 3)) reader.readVector2() else Vector2.Zero
}

class SpriteRenderData internal constructor(reader: ObjectReader) {
    val texture = PPtr<Texture2D>(reader)
    val alphaTexture = if (reader.unityVersion >= intArrayOf(5, 2)) PPtr<Texture2D>(reader) else null
    val secondaryTexturesSize = if (reader.unityVersion[0] >= 2019) {
        reader.readArrayOf { SecondarySpriteTexture(reader) }
    } else emptyArray()
    val mSubMeshes: Array<SubMash>
    val mIndexBuffer: ByteArray
    val mVertexData: VertexData?
    val vertices: Array<SpriteVertex>
    val indices: Array<UShort>
    val mBindPose: Array<Matrix4x4>
    val mSourceSkin: Array<BoneWeights4>
    val textureRect: Rectangle
    val textureRectOffset: Vector2
    val atlasRectOffset: Vector2
    val settingsRaw: SpriteSettings
    val uvTransform: Vector4
    val downScaleMultiplier: Float

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(5, 6)) {
            mSubMeshes = reader.readArrayOf { SubMash(reader) }
            mIndexBuffer = reader.readNextByteArray()
            reader.alignStream()
            mVertexData = VertexData(reader)
            vertices = emptyArray()
            indices = emptyArray()
        } else {
            vertices = reader.readArrayOf { SpriteVertex(reader) }
            indices = reader.readNextUShortArray()
            reader.alignStream()
            mSubMeshes = emptyArray()
            mIndexBuffer = byteArrayOf()
            mVertexData = null
        }
        if (version[0] >= 2018) {
            mBindPose = reader.readNextMatrixArray()
            mSourceSkin = if (version[0] == 2018 && version[1] < 2) {
                reader.readArrayOf { BoneWeights4(reader) }
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
        settingsRaw = SpriteSettings(reader)
        uvTransform = if (version >= intArrayOf(4, 5)) {
            reader.readVector4()
        } else Vector4.Zero
        downScaleMultiplier = if (version[0] >= 2017) {
            reader.readFloat()
        } else 0f
    }
}