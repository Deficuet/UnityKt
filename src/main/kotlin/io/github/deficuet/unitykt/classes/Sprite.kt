package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.enums.NumericalEnum
import io.github.deficuet.unitykt.enums.NumericalEnumCompanion
import io.github.deficuet.unitykt.math.*
import java.awt.image.BufferedImage
import java.util.*

interface Sprite: NamedObject {
    val mRect: Rectangle
    val mOffset: Vector2
    val mBorder: Vector4
    val mPixelsToUnits: Float
    val mPivot: Vector2
    val mExtrude: UInt
    val mIsPolygon: Boolean
    val mRenderDataKey: Pair<UUID, Long>
    val mAtlasTags: Array<out String>
    val mSpriteAtlas: PPtr<SpriteAtlas>?
    val mRD: SpriteRenderData
    val mPhysicsShape: Array<out Array<out Vector2>>

    fun getImage(): BufferedImage?
}

interface SecondarySpriteTexture {
    val texture: PPtr<Texture2D>
    val name: String
}

enum class SpritePackingRotation(override val id: UInt): NumericalEnum<UInt> {
    None(0u),
    FlipHorizontal(1u),
    FlipVertical(2u),
    Rotate180(3u),
    Rotate90(4u);

    companion object: NumericalEnumCompanion<UInt, SpritePackingRotation>(values(), None)
}

enum class SpritePackingMode(override val id: UInt): NumericalEnum<UInt> {
    Tight(0u),
    Rectangle(1u);

    companion object: NumericalEnumCompanion<UInt, SpritePackingMode>(values(), Tight)
}

enum class SpriteMeshType(override val id: UInt): NumericalEnum<UInt> {
    FullRect(0u),
    Tight(1u);

    companion object: NumericalEnumCompanion<UInt, SpriteMeshType>(values(), FullRect)
}

interface SpriteSettings {
    val settingsRaw: UInt
    val packed: UInt
    val packingMode: SpritePackingMode
    val packingRotation: SpritePackingRotation
    val meshType: SpriteMeshType
}

interface SpriteVertex {
    val pos: Vector3
    val uv: Vector2
}

interface SpriteRenderData {
    val texture: PPtr<Texture2D>
    val alphaTexture: PPtr<Texture2D>?
    val secondaryTexturesSize: Array<out SecondarySpriteTexture>
    val mSubMeshes: Array<out SubMash>
    val mIndexBuffer: ByteArray
    val mVertexData: VertexData?
    val vertices: Array<out SpriteVertex>
    val indices: Array<out UShort>
    val mBindPose: Array<out Matrix4x4>
    val mSourceSkin: Array<out BoneWeights4>
    val textureRect: Rectangle
    val textureRectOffset: Vector2
    val atlasRectOffset: Vector2
    val settingsRaw: SpriteSettings
    val uvTransform: Vector4
    val downScaleMultiplier: Float
}
