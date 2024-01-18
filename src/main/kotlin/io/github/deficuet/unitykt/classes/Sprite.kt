package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.enums.NumericalEnum
import io.github.deficuet.unitykt.enums.NumericalEnumCompanion
import io.github.deficuet.unitykt.math.*
import io.github.deficuet.unitykt.pptr.PPtr
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

    fun getTexture2D(): Texture2D?

    /**
     * @return The cropped [BufferedImage] which has the same orientation as the parent [Texture2D].
     *
     * i.e. usually up-side-down.
     */
    fun getImage(strategy: SpriteCropStrategy = SpriteCropStrategy.USE_TEXTURE_RECT): BufferedImage?
}

/**
 * Has effect only when cropping the image using [mRD][Sprite.mRD].
 *
 * i.e. when the `PPtr` [mSpriteAtlas][Sprite.mSpriteAtlas] gets `null`.
 */
enum class SpriteCropStrategy {
    /**
     * Use [textureRect][SpriteRenderData.textureRect]
     */
    USE_TEXTURE_RECT,
    /**
     * Use [mRect][Sprite.mRect]
     */
    USE_RECT
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
