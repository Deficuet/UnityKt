package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Rectangle
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.pptr.PPtr
import java.util.*

interface SpriteAtlas: NamedObject {
    val mPackedSprites: Array<out PPtr<Sprite>>
    val mRenderDataMap: Map<Pair<UUID, Long>, SpriteAtlasData>
    val mIsVariant: Boolean
}

interface SpriteAtlasData {
    val texture: PPtr<Texture2D>
    val alphaText: PPtr<Texture2D>
    val textureRect: Rectangle
    val textureRectOffset: Vector2
    val atlasRectOffset: Vector2
    val uvTransform: Vector4
    val downScaleMultiplier: Float
    val settingsRaw: SpriteSettings
    val secondaryTextures: Array<out SecondarySpriteTexture>
}
